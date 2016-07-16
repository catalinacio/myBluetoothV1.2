package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

/**
 * this has to be  done whenever i Click the Connect
 * <p>
 * inside Connect, I got to get the Device address through listed devices.
 * <p>
 * to get the device appear in the devices list I have to Search. whenever I find it i request Pairing and then
 * I should clear the found list and add it later to PAIRED LIST.
 * SO WHEN I CLICK ON PAIRED LIST LINE, I GET THE DEVICE ADDRESS INTO A STRING global
 * <p>
 * then when i click connect, it attempts connect to THAT remoteDEVICE Address.
 * if connection succceds I can Send activity for result back. SO i can send commends to BT.
 * aka CHAR/BYTE
 */



public class MyBtActivity extends AppCompatActivity {
    private static final char CENTERSERVOPOSITION = 'x';
    private static final char MOVECARTOBACK = 'b';

    private static final char MOVECARTOFRONT = 'f';
    String DEVICE_ADDRESS = null;
    private char BLINK = '1';
    private char LEFTDRRIVESERVO = 'l';
    private char RIGHTDRIVESERVO = 'r';

    private final String MESSAGE_MOVE = "1";
    private final String myDEVICE_ADDRESS = "20:15:02:13:11:03";
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

        /**
         *
         * HAS TO BE DELETED FROM HERE AND PUT INTO CONNECT
         */
    //    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(myDEVICE_ADDRESS);
      //  mConnectThread = new ConnectThread(mBluetoothDevice);
        //mConnectThread.start();



    }

    public void onClickSend(View view) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(myDEVICE_ADDRESS);
        mConnectThread = new ConnectThread(mBluetoothDevice);
        mConnectThread.start();

        sendMessage();


        Toast.makeText(getApplicationContext(), "Send f to arduino", Toast.LENGTH_SHORT).show();

    }

    private void sendMessage() {
        byte[] send = MESSAGE_MOVE.getBytes();
        mConnectedThread.write(send);

    }

    public void sendCharMessage(View view) {
        mConnectedThread.mywrite(BLINK);


    }

    public void sendLEFTSERVOCharMessage(View view) {
        mConnectedThread.mywrite(LEFTDRRIVESERVO);


    }

    public void sendRIGHTCharMessage(View view) {
        mConnectedThread.mywrite(RIGHTDRIVESERVO);
    }

    public void sendCENTERCharMessage(View view) {
        mConnectedThread.mywrite(CENTERSERVOPOSITION);
    }

    public void sendFRONTCharMessage(View view) {
        mConnectedThread.mywrite(MOVECARTOFRONT);
    }

    public void sendBACKRCharMessage(View view) {
        mConnectedThread.mywrite(MOVECARTOBACK);
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        actionItems();


    }

    private void actionItems() {

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             //     mBluetoothDevice = mBluetoothAdapter.getRemoteDevice();
                DEVICE_ADDRESS = newFoundBtList.get(i).toString().substring(newFoundBtList.get(i).toString().length() - 17);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

                Toast.makeText(getApplicationContext(), mBluetoothDevice.getAddress() + " got if from REMOTE", Toast.LENGTH_SHORT).show();

            }
        });

        listNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DEVICE_ADDRESS = newFoundBtList.get(i).toString().substring(newFoundBtList.get(i).toString().length() - 17);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS); // i recover the bt dev from adapter

                connectDiscoveredDevice(mBluetoothDevice);
                Toast.makeText(getApplicationContext(), mBluetoothDevice.getAddress() + " got NEW DEV if from REMOTE", Toast.LENGTH_SHORT).show();

            }
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;
            switch (msg.what) {
                case 1
                        :
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }
        }
    };

    private void connectDiscoveredDevice(BluetoothDevice bt) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        mConnectThread = new ConnectThread(bt);
        mConnectThread.start();
        mConnectedThread.start();
        sendMessage();
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

    public void connectH506(View view) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(myDEVICE_ADDRESS);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        mConnectThread = new ConnectThread(mBluetoothDevice);
        mConnectThread.start();

        Toast.makeText(getApplicationContext(), mConnectThread.getState().toString(), Toast.LENGTH_SHORT).show();
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

            mConnectedThread = new ConnectedThread(mmSocket);
            try {
                mConnectedThread.start();
            } catch (IllegalThreadStateException e) {
                Log.e(TAG, "error");

            }

            // Do work to manage the connection (in a separate thread)
            // manageConnectedSocket(mmSocket);
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

    private void manageConnectedSocket(BluetoothSocket socket) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
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
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length
                            -
                            bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            mHandler.obtainMessage(
                                    1
                                    , begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes
                                    -
                                    1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
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

        public void mywrite(char letter) {
            try {
                mmOutStream.write(letter);
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
