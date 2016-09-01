package com.yiyang.ht66f50_bt_voltmeter12;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final int REQUEST_ENABLE_BT_DISCOVERABLE = 1235;
    private static final int BT_MESSAGE_READ = 1236;
    private static final String BT_SDP_SERVICE_NAME = "BluetoothTest";
    private static final String BT_SDP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private Button btnConnect;
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private String[] deviceList;
    private ConnectedThread connectedThread;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_MESSAGE_READ:
                    tvValue.setText("Value: " + msg.arg1);
                    tvVolt.setText(String.format("Volt: %5.2f V", msg.arg1 * 5.0 / 4096));
                    break;
            }
        }
    };
    private TextView tvValue;
    private TextView tvVolt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);


        btAdapter = BluetoothAdapter.getDefaultAdapter();

        tvValue = (TextView) findViewById(R.id.tvValue);
        tvVolt = (TextView) findViewById(R.id.tvVolt);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnConnect.getText().toString().equals("BT Connect")) {
                    btnConnect.setText("BT Disconnect");


                    String deviceListString = "";
                    pairedDevices = btAdapter.getBondedDevices();
                    // If there are paired devices
                    if (pairedDevices.size() > 0) {
                        // Loop through paired devices
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            if (device.getAddress().length() > 0)
                                deviceListString += device.getName() + "\n" + device.getAddress() + "\n\n";
                        }
                    }


                    deviceList = deviceListString.split("\n\n");
                    //Select BT Server
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    Dialog dialog = builder.setTitle("Select BT Device")
                            .setItems(deviceList, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "Connect to " + deviceList[i], Toast.LENGTH_LONG).show();
                                    connectToDevice(deviceList[i]);
                                }
                            })
                            .create();
                    dialog.show();

                } else {
                    btnConnect.setText("BT Connect");

                    //Close connection
                    connectedThread.cancel();

                }
            }
        });
    }

    private void connectToDevice(String deviceInfo) {
        String[] items = deviceInfo.split("\n");
        BluetoothDevice btDevice = null;
        for (BluetoothDevice btd :
                pairedDevices) {
            if (btd.getAddress().equals(items[1])) {
                btDevice = btd;
                break;
            }
        }

        if (btDevice != null) {
            ConnectThread connectThread = new ConnectThread(btDevice);
            connectThread.start();
        }

    }


    private void manageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
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
                // BT_SDP_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(BT_SDP_UUID));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

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
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    int v = mmInStream.read();
                    v += mmInStream.read() * 256;
                    mHandler.obtainMessage(BT_MESSAGE_READ, v, -1, null).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
