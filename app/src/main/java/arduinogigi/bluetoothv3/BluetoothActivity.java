package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
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

public class BluetoothActivity extends AppCompatActivity {
    //  ArrayList<BluetoothDevice> devices;

    ListView listPairedDevices;
    ListView listNewDevices;
    ArrayList pairedList =new ArrayList();
    ArrayList newFoundBtList =new ArrayList();
    Button pairedButton;
    Button scanButton;
    BluetoothDevice mBluetoothDevice;
    BluetoothAdapter mBluetoothAdapter;
    //ArrayAdapter;
    //
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothSocket msocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

            init();


    }

    private void init() {
        pairedButton = (Button) findViewById(R.id.button_paired);
        scanButton = (Button) findViewById(R.id.button_scan);
        listPairedDevices = (ListView) findViewById(R.id.listPaired_Devices);
        listNewDevices = (ListView) findViewById(R.id.list_New_BT_Devices);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
   /*
    My listener for button click
   */

        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBtPairedList();

            }
        });
        scanButton.setOnClickListener(new View.OnClickListener() {
            //have to pair dev found
            @Override
            public void onClick(View view) {
                scanNewBtsLists();
            }
        });
        listPairedDevices.setOnItemClickListener(
                //Have to connect and send
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String example = String.valueOf(adapterView.getItemAtPosition(i));

                        Toast.makeText(getApplicationContext(), "not yet", Toast.LENGTH_SHORT).show();


                    }

                });

    }

    /*
   *
            My Start send
   *
     */
    public void onClickSend(View view) {

        testMyDeviceList();
    }

    private void testMyDeviceList() {
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);
        listPairedDevices.setAdapter(arrayAdapter);
    }

        /*
       // connectAndSend();
      //  beginListenForData();

        String string = "f".toString();
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), string,Toast.LENGTH_SHORT).show();
        //textView.append("\nSent Data:"+string+"\n");
*/




        //private void getd
    /*

        private functions

     */

    private void showBtPairedList() {
        /*used to see if i have @param paireddevices on my phone
        // If there are paired devices
        // Loop through paired devices
        // Add the name and address to an array adapter to show in a ListView
            */
        pairedList =new ArrayList();
        if (mBluetoothAdapter.getBondedDevices() == null) {
            Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_SHORT).show();
        }

        else {
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
                    if (addDeviceToRepository(device))

                    newFoundBtList.add(device.getName() + "\n" + device.getAddress());
                    listPairedDevices.setAdapter(arrayAdapter2);
                    addDeviceToRepository(device);
                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


    }

    private boolean addDeviceToRepository(BluetoothDevice device) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        boolean ok = false;
        final ArrayAdapter arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);
        for (BluetoothDevice bt : pairedDevices) {
            String s = bt.getAddress().toString();
            if (!device.getAddress().toString().contains(s))
                pairedList.add(device.getName() + "\n" + device.getAddress());
            listPairedDevices.setAdapter(arrayAdapter2);
            ok = true;
            break;
        }
        return ok;
    }


}
