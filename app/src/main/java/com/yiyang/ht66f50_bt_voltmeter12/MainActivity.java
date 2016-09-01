package com.yiyang.ht66f50_bt_voltmeter12;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final int REQUEST_ENABLE_BT_DISCOVERABLE = 1235;
    private static final int BT_MESSAGE_READ = 1236;
    private static final String BT_SDP_SERVICE_NAME = "BluetoothTest";
    private static final String BT_SDP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnConnect.getText().toString().equals("BT Connect")){
                    btnConnect.setText("BT Disconnect");

                    //Select BT Server


                    //Connect to BT Server

                    //Update data asynchronously

                }else{
                    btnConnect.setText("BT Connect");

                    //Close connection



                }
            }
        });
    }
}
