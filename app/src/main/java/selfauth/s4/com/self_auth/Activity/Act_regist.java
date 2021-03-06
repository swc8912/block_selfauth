package selfauth.s4.com.self_auth.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import Database.MyDatabaseOpenHelper;
import selfauth.s4.com.self_auth.R;

public class Act_regist extends AppCompatActivity {
    private final String TAG = "log_actRegist";
    private ArrayList<String> device_set;
    private ListView listview;
    private CustomListViewAdapter adapter;

    private Button btn_scan;
    private boolean isScaning = false;
    private Button btn_regist;

    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBlueToothAdapter;

    private ArrayList<CustomListViewItem> selectedItem;

    //---------------- db
    private MyDatabaseOpenHelper helper;
    private SQLiteDatabase database;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                Log.i("TEST", "find = " + device.getName());
                if(device_set.contains(device.getName()) == false && !TextUtils.isEmpty(device.getName())) {
                    Log.i("TEST", "new = " + device.getName());
                    device_set.add(device.getName());
                    //adapter.add(new CustomListViewItem(0, device.getName(), true )); 수정해야함. 이미 연결했었나 여부 판단

                    adapter.add(new CustomListViewItem(0, device.getName(), device.getAddress(), false ));
                    adapter.notifyDataSetChanged();
                }


                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    Log.i("TEST", "이미 연결된 " + device.getName());
                }

                //mBlueToothAdapter.cancelDiscovery();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_regist);

        //------ view setting
        setViews();
        setListener();

        device_set = new ArrayList<String>();
        selectedItem = new ArrayList<CustomListViewItem>();

        //-------- database
        helper = new MyDatabaseOpenHelper(Act_regist.this, MyDatabaseOpenHelper.tableName_keys, null, 1);
        database = helper.getWritableDatabase();


        //-------- test
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        listview.setAdapter(adapter);
    }

    public void setViews(){
        btn_regist=(Button)findViewById(R.id.act_regist_btn2);
        btn_scan=(Button)findViewById(R.id.act_regist_btn1);
        adapter = new CustomListViewAdapter(this);
        listview=(ListView)findViewById(R.id.act_regist_listview);

    }
    public void setListener(){
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isScaning) {
                    Intent bluetoothsearchintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(bluetoothsearchintent, REQUEST_ENABLE_BT);

                    mBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBlueToothAdapter.startDiscovery();

                    btn_scan.setText("검색중지");
                    isScaning=true;
                }
                else{
                    mBlueToothAdapter.cancelDiscovery();
                    btn_scan.setText("장비검색하기");
                    isScaning=false;
                }
            }
        });

        //------------------- 수정해야함
        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result="";
                for(CustomListViewItem item : adapter.getSelectedItems()){
                    result+=item.getText()+" / ";
                }
                Log.i(TAG, result);

                for(CustomListViewItem item : adapter.getSelectedItems()){
                    helper.insertIntoSelected(database, item.getAddr());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isScaning){
            mBlueToothAdapter.cancelDiscovery();
        }
    }
}

