package com.example.iotsmarthome;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText editTextBroker;
    EditText editTextTopic;
    Button buttonConnect;
    Button buttonPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
