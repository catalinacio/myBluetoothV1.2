package arduinogigi.bluetoothv3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

    ListView listPairedDevices;
    ListView listNewDevices;
    ArrayList pairedList =new ArrayList();
    ArrayList newFoundBtList =new ArrayList();
    Button pairedButton;
    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        pairedButton = (Button) findViewById(R.id.button_paired);
        scanButton = (Button) findViewById(R.id.button_scan);
        listPairedDevices = (ListView) findViewById(R.id.list_Paired_BT_devices);
        listNewDevices = (ListView) findViewById(R.id.list_New_BT_Devices);

        Toast.makeText(getApplicationContext(),"huh1",Toast.LENGTH_SHORT).show();
    }
}
