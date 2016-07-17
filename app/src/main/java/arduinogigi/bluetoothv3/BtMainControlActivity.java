package arduinogigi.bluetoothv3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class BtMainControlActivity extends AppCompatActivity {
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 0;
    private final String DEVICE_ADDRESS = "20:15:02:13:11:03";

    private final String myDEVICE_ADDRESS = "20:15:02:13:11:03";
    BluetoothDevice mBluetoothDevice;
    BluetoothAdapter mBluetoothAdapter;

    /**
     *
     */
    Switch switch_Btn;
    ImageView imageView;
    /**
     *
     */
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button startButton, sendButton, clearButton, stopButton;
    TextView textView;
    EditText editText;
    boolean deviceConnected = false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_main);
        switch_Btn = (Switch) findViewById(R.id.switch_BT);
        imageView = (ImageView) findViewById(R.id.imageView);

        startButton = (Button) findViewById(R.id.buttonStart);
        actionInit();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    private void actionInit() {
        imageView.setVisibility(View.INVISIBLE);
        switch_Btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (!b) {
                    disable_Bluetooth();
                } else {
                    enable_Bluetooth();
                }

            }
        });
    }

    private void disable_Bluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
              mBluetoothAdapter.disable();
        }
    }

    private void enable_Bluetooth() {
        CheckBt();
        // to add swtich activated()
      //  if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      //  }
    }

    private void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
                    Toast.LENGTH_SHORT).show();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth null !", Toast.LENGTH_SHORT).show();
        }
    }
    public void onClickSelectDeviceImage(View view) {
        imageView.setVisibility(View.VISIBLE);

    }

    /**
     *
     *
     */


    public boolean myDeviceTestSTART(View view) {
        Intent serverIntent = new Intent(this, BluetoothActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        return true;
    }

    public void actionControlLaunch(View view) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //  alertDialogBuilder.setNegativeButton("No");
        //  alertDialogBuilder.setPositiveButton("Yes");
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imageView.setVisibility(View.INVISIBLE);
                try {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter.isEnabled()) {

                        launchMyBtActivity();
                    }
                    if(!mBluetoothAdapter.isEnabled())
                        Toast.makeText(getApplicationContext(),"Activate Bt",Toast.LENGTH_SHORT).show();
                }
                catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {

                    Toast.makeText(getApplicationContext(),"cancelled",Toast.LENGTH_SHORT).show();
                     imageView.setVisibility(View.INVISIBLE);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        alertDialogBuilder.setMessage("do you want to launch this?");
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void launchMyBtActivity() {
        Intent intent = new Intent(this, MyBtActivity.class);
        startActivity(intent);
    }



    /**
     * My addings
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address

                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(myDEVICE_ADDRESS);
                    // Attempt to connect to the device
                    //Connect
                    Toast.makeText(getApplicationContext(), "Connect now ", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }



    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }
    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    textView.append(string);
                                }
                            });

                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void onClickSend(View view) {
        String string = editText.getText().toString();
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.append("\nSent Data:" + string + "\n");

    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        //setUiEnabled(false);
        deviceConnected = false;
        textView.append("\nConnection Closed!\n");
    }


}

