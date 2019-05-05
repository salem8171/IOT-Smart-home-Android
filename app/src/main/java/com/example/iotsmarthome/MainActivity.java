package com.example.iotsmarthome;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText editTextBroker;
    EditText editTextTopic;
    Button buttonConnect;
    Button buttonPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkSniffTask networkSniffTask = new NetworkSniffTask(this);
        networkSniffTask.execute();

        editTextBroker = findViewById(R.id.editText_broker);
        editTextTopic = findViewById(R.id.editText_topic);
        buttonConnect = findViewById(R.id.button_connect);
        buttonPublish = findViewById(R.id.button_publish);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttServiceIntent = new Intent(MainActivity.this, MqttService.class);
                mqttServiceIntent.putExtra(MqttService.EXTRA_MQTT_SERVER_DOMAIN, editTextBroker.getText().toString());
                startService(mqttServiceIntent);
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttPublishIntent = new Intent(MqttService.ACTION_MQTT_PUBLISH);
                mqttPublishIntent.putExtra(MqttService.EXTRA_MQTT_TOPIC, editTextTopic.getText().toString());
                mqttPublishIntent.putExtra(MqttService.EXTRA_MQTT_MESSAGE, "Hello from android");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(mqttPublishIntent);
            }
        });

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MqttService.EXTRA_MQTT_MESSAGE), Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter(MqttService.ACTION_MQTT_SUBSCRIBE));

    }

    static class NetworkSniffTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "nstask";

        private WeakReference<Context> mContextRef;

        NetworkSniffTask(Context context) {
            mContextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "Let's sniff the network");

            try {
                Context context = mContextRef.get();

                if (context != null) {

                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    WifiInfo connectionInfo = wm.getConnectionInfo();
                    int ipAddress = connectionInfo.getIpAddress();
                    String ipString = Formatter.formatIpAddress(ipAddress);


                    Log.d(TAG, "activeNetwork: " + (activeNetwork));
                    Log.d(TAG, "ipString: " + (ipString));

                    String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                    Log.d(TAG, "prefix: " + prefix);

                    for (int i = 0; i < 255; i++) {
                        String testIp = prefix + (i);

                        InetAddress address = InetAddress.getByName(testIp);
                        boolean reachable = address.isReachable(1000);
                        String hostName = address.getCanonicalHostName();

                        if (reachable)
                        {
                            Log.i(TAG, "Host: " + (hostName) + "(" + (testIp) + ") is reachable!");
                            try {
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(testIp, 1883), 1000);
                                socket.close();
                                Log.i(TAG, "Host: " + (hostName) + "(" + (testIp) + ") has mqtt port open");
//                                Toast.makeText(MainActivity.this, "has mqtt port open", Toast.LENGTH_LONG).show();
                            }

                            catch(ConnectException ce){
                                ce.printStackTrace();
                            }

                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }

            return null;
        }
    }
}
