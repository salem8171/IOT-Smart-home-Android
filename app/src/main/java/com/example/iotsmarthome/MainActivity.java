package com.example.iotsmarthome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    EditText editTextBroker;
    EditText editTextTopic;
    Button buttonConnect;
    Button buttonPublish;

    MqttAndroidClient mqttClient;

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
                mqttClient = new MqttAndroidClient(MainActivity.this, "tcp://" + editTextBroker.getText() + ":1883", MqttClient.generateClientId());

                try
                {
                    mqttClient.connect().setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Toast.makeText(MainActivity.this, "Connected successfully to the mqtt broker", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Toast.makeText(MainActivity.this, "Failed to connect to the mqtt broker", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (MqttException exception)
                {
                    Toast.makeText(MainActivity.this, "MqttException occured", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    MqttMessage message = new MqttMessage(new String("Hello").getBytes());
                    mqttClient.publish(editTextTopic.getText().toString(), message);
                } catch (MqttException exception)
                {
                    Toast.makeText(MainActivity.this, "MqttException occured", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
