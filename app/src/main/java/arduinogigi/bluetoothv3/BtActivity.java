package arduinogigi.bluetoothv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BtActivity extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver mReceiver;
    Set<BluetoothDevice> pairedDevices;
    ListView listPairedDevices;
    ListView listNewDevices;
    ArrayList pairedList =new ArrayList();
    ArrayList newFoundBtList =new ArrayList();
    Button pairedButton;
    Button scanButton;
String s="blabla";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        pairedButton = (Button) findViewById(R.id.button_paired);
       scanButton = (Button) findViewById(R.id.button_scan);

           // pairedList.add(s.toString());

        listPairedDevices = (ListView) findViewById(R.id.list_Paired_BT_devices);
        listNewDevices = (ListView) findViewById(R.id.list_New_BT_Devices);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,pairedList);

        listNewDevices.setAdapter(adapter);


        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBtPairedList(view);
            }
        });

/*
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showNewBTList(v);

            }
        });
*/
        listPairedDevices.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String example=String.valueOf(adapterView.getItemAtPosition(i));
                        Toast.makeText(getApplicationContext(),"clicked the "+example,Toast.LENGTH_SHORT).show();
                    }
                }

        );
    }
/*
    private void showNewBTList(View v) {
        doDiscovery();
        v.setVisibility(View.GONE);
        discover_BT(v);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newFoundBtList);
        listPairedDevices.setAdapter(arrayAdapter);
    }
*/
    public void showBtPairedList(View view) {

                pairedList =new ArrayList();
                if (mBluetoothAdapter.getBondedDevices() == null)
                    Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(), "smth", Toast.LENGTH_SHORT).show();
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

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
       // findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
           // Toast.makeText(getApplicationContext(),"visible text new devices",Toast.LENGTH_SHORT).show();
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();

        Toast.makeText(getApplicationContext(),mBluetoothAdapter.getName().toString()+mBluetoothAdapter.getAddress(),Toast.LENGTH_LONG).show();
    }



    public void discover_BT(View view) {
        //listBT(view);
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView

                    newFoundBtList.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothAdapter!= null )
        {
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
        //mBluetoothAdapter.disable();
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
  /*  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };*/

}
