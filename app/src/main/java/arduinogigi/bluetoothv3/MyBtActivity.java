package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MyBtActivity extends AppCompatActivity {
    String DEVICE_ADDRESS = null;
    private static final String TAG = "MyBtActivity";
    private static final int MESSAGE_READ = 0;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    Set<BluetoothDevice> devicePairedRepository;
    ListView listPairedDevices;
    ListView listNewDevices;
    Button pairedButton;
    Button scanButton;
    ArrayList pairedList = new ArrayList();
    ArrayList newFoundBtList = new ArrayList();

    BluetoothDevice mBluetoothDevice;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
    }

    public void onClickSend(View view) {
        Toast.makeText(getApplicationContext(), "Send f to arduino", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);

    }


    private void init() {
        pairedButton = (Button) findViewById(R.id.button_paired);
        scanButton = (Button) findViewById(R.id.button_scan);
        listPairedDevices = (ListView) findViewById(R.id.listPaired_Devices);
        listNewDevices = (ListView) findViewById(R.id.list_New_BT_Devices);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBtPairedList();
            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNewBtsLists();
            }
        });

        actionItems();


    }

    private void actionItems() {

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //the device adress is taken from the array and you substract the last 17 letters representing the MAC;
                // XX : XX : XX : XX : XX : XX mac form type
                DEVICE_ADDRESS = pairedList.get(i).toString().substring(pairedList.get(i).toString().length() - 17);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS); // i recover the bt dev from adapter
                //Toast.makeText(getApplicationContext(),mBluetoothDevice.getAddress()+" got if from REMOTE",Toast.LENGTH_SHORT).show();

                connectDiscoveredDevice(mBluetoothDevice);
                if (Thread.currentThread().isAlive()) {
                    Toast.makeText(getApplicationContext(), "migh bve goncet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*this dont cursh bunt neeeds separate thread
        if( testDeviceAdress(DEVICE_ADDRESS)) {
                  Toast.makeText(getApplicationContext(),mBluetoothDevice.getAddress()+" got it from dev",Toast.LENGTH_SHORT).show();
                  try {
                      socket = mBluetoothDevice.createRfcommSocketToServiceRecord(PORT_UUID);
                      socket.connect();
                  }
                  catch (Exception ConnectException ) {
                      Log.e(TAG,"connect error");
                  }

                  }
              else{
                  Toast.makeText(getApplicationContext(),"device adress is a string "+ DEVICE_ADDRESS,Toast.LENGTH_SHORT).show();
              }
         */
        listNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DEVICE_ADDRESS = newFoundBtList.get(i).toString().substring(newFoundBtList.get(i).toString().length() - 17);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS); // i recover the bt dev from adapter

                connectDiscoveredDevice(mBluetoothDevice);
                Toast.makeText(getApplicationContext(), mBluetoothDevice.getAddress() + " got NEW DEV if from REMOTE", Toast.LENGTH_SHORT).show();

                // Toast.makeText(getApplicationContext(),"Clicked new Device To pair"+DEVICE_ADDRESS,Toast.LENGTH_SHORT).show();
                // These lines are Setting the address to device adress then return to main activity;
                /*
                Intent intent = new Intent();
                intent.putExtra(DEVICE_ADDRESS, DEVICE_ADDRESS);
                // Set result and finish this Activity
                setResult(MyBtActivity.RESULT_OK, intent);
                finish();*/
            }
        });
    }

    private void connectDiscoveredDevice(BluetoothDevice bt) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        mConnectThread = new ConnectThread(bt);
        mConnectThread.start();
    }

    private boolean testDeviceAdress(String s) {
        boolean test = false;
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice bt : bondedDevices) {
                if (bt.getAddress().equals(s)) {
                    mBluetoothDevice = bt;
                    test = true;
                    break;
                }
            }

        }
        return test;
    }


    private void scanNewBtsLists() {
        newFoundBtList = new ArrayList();
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        // Turn on sub-title for new devices
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();


        // Create a BroadcastReceiver for ACTION_FOUND

// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        final ArrayAdapter arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newFoundBtList);
        listNewDevices.setAdapter(arrayAdapter2);
        //        IntentFilter filter2=new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        //  registerReceiver(mReceiver,filter2);

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                newFoundBtList.add(device.getName() + "\n" + device.getAddress());
                //devicesRepository.add(device);
            } else if (BluetoothDevice.ACTION_FOUND.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                intent.setAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                device.setPairingConfirmation(true);
            }
        }
    };

    private void showBtPairedList() {
        /*used to see if i have @param paireddevices on my phone
        // If there are paired devices
        // Loop through paired devices
        // Add the name and address to an array adapter to show in a ListView
            */
        pairedList = new ArrayList();
        if (mBluetoothAdapter.getBondedDevices() == null) {
            Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_SHORT).show();
        } else {
            devicePairedRepository = mBluetoothAdapter.getBondedDevices();
            if (devicePairedRepository.size() > 0) {
                for (BluetoothDevice device : devicePairedRepository) {
                    pairedList.add(device.getName().toString() + "\n" + device.getAddress().toString());
                    //devicesRepository.add(device);
                    //devices.add(device); // this can be missed
                    ;
                }
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);
        listPairedDevices.setAdapter(arrayAdapter);
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    // Handler mHandler;
                    //  mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        //Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
