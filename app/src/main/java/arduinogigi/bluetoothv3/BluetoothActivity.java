package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    ListView listPairedDevices;
    ListView listNewDevices;
    ArrayList pairedList =new ArrayList();
    ArrayList newFoundBtList =new ArrayList();
    Button pairedButton;
    Button scanButton;

    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        pairedButton = (Button) findViewById(R.id.button_paired);
        scanButton = (Button) findViewById(R.id.button_scan);
        listPairedDevices = (ListView) findViewById(R.id.listPaired_Devices);
        listNewDevices = (ListView) findViewById(R.id.list_New_BT_Devices);

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
            @Override
            public void onClick(View view) {
                scanNewBtsList();
            }
        });


    }

    private void scanNewBtsList() {
        doDiscovery();
    }



    /*

        private functions

     */

    private void showBtPairedList() {
        pairedList =new ArrayList();

        if (mBluetoothAdapter.getBondedDevices() == null)
            Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_SHORT).show();
        else {
          //used to see if i have paireddevices on my phone
            //  Toast.makeText(getApplicationContext(), "smth", Toast.LENGTH_SHORT).show();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    pairedList.add(device.getName().toString() + "\n" + device.getAddress().toString());
                }
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedList);
        listPairedDevices.setAdapter(arrayAdapter);
    }

    private void doDiscovery() {
        // if (D) Log.d(TAG, "doDiscovery()");
    newFoundBtList=new ArrayList();
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        // Turn on sub-title for new devices        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
        //Toast.makeText(getApplicationContext(),mBluetoothAdapter.getName().toString()+mBluetoothAdapter.getAddress(),Toast.LENGTH_LONG).show();
        /*

         */
        final  ArrayAdapter arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newFoundBtList);

        // Create a BroadcastReceiver for ACTION_FOUND
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView

                    newFoundBtList.add(device.getName() + "\n" + device.getAddress());
                    listPairedDevices.setAdapter(arrayAdapter2);

                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


    }



}
