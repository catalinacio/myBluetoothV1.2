package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class BluetoothActivity extends AppCompatActivity {
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private final String myDEVICE_ADDRESS = "20:15:02:13:11:03";


    private static final char CENTERSERVOPOSITION = 'x';
    private static final char MOVECARTOBACK = 'b';
    private static final char MOVECARTOFRONT = 'f';
    private static final String TAG = "ds";
    private char BLINK = '1';
    private char LEFTDRRIVESERVO = 'l';
    private char RIGHTDRIVESERVO = 'r';

    /***/


    ListView listPairedDevices;
    ListView listNewDevices;
    ArrayList pairedList = new ArrayList();
    ArrayList newFoundBtList = new ArrayList();
    Button pairedButton;
    Button scanButton;
    Button left_btn, right_btn, front_btn, back_btn, connect_btn, test_btn;
    BluetoothDevice mBluetoothDevice;
    BluetoothAdapter mBluetoothAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_final_activity);

        init();



    }

    private void init() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        left_btn = (Button) findViewById(R.id.btn_left);
        right_btn = (Button) findViewById(R.id.btn_rigtht);
        front_btn = (Button) findViewById(R.id.btn_front);
        back_btn = (Button) findViewById(R.id.btn_back);
        connect_btn = (Button) findViewById(R.id.btn_connect);


      //  test_btn = (Button) findViewById(R.id.btn_test_my_things);

       /* test_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                while(test_btn.isPressed())
                {
                    mConnectedThread.mywrite(MOVECARTOBACK);
                }
                return false;
            }
        });
    }

    private void doACtion() {
        while(test_btn.isPressed())
        {
            mConnectedThread.mywrite(MOVECARTOBACK);
        }
    }*/


    /**
     * My listener for button click
     */


    public void sendLEFTSERVOCharMessage(View view) {
        mConnectedThread.mywrite(LEFTDRRIVESERVO);
    }

    public void sendRIGHTSERVOCharMessage(View view) {
        mConnectedThread.mywrite(RIGHTDRIVESERVO);
    }

    public void sendCENTERSERVOPOSITIONCharMessage(View view) {
        mConnectedThread.mywrite(CENTERSERVOPOSITION);
    }

    public void sendFRONTCharMessage(View view) {
        mConnectedThread.mywrite(MOVECARTOFRONT);
    }

    public void sendBACKRCharMessage(View view) {
        mConnectedThread.mywrite(MOVECARTOBACK);
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

    public void sendCharMessages(View view) {
        mConnectedThread.mywrite(MOVECARTOBACK);
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
                       /* mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                        begin = i + 1;
                        if (i == bytes
                                -
                                1) */
                            {
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
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    pairedList.add(device.getName().toString() + "\n" + device.getAddress().toString());
                    //devices.add(device); // this can be missed
                    // addDevicesToMyList(device);
                }

            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);
        listPairedDevices.setAdapter(arrayAdapter);
    }

    private void addDevicesToMyList(BluetoothDevice device) {

        //devices.add(device);
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
        final ArrayAdapter arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);

        // Create a BroadcastReceiver for ACTION_FOUND
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    //  if (addDeviceToRepository(device))

                    newFoundBtList.add(device.getName() + "\n" + device.getAddress());
                    listPairedDevices.setAdapter(arrayAdapter2);
                    // addDeviceToRepository(device);
                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


    }


}
