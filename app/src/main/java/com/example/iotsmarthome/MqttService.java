package com.example.iotsmarthome;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttService extends Service {

    public static final String EXTRA_MQTT_SERVER_DOMAIN = "mqtt server domain";
    public static final String EXTRA_MQTT_TOPIC = "mqtt topic";
    public static final String EXTRA_MQTT_MESSAGE = "mqtt message";
    public static final String ACTION_MQTT_PUBLISH = "mqtt publish";
    public static final String ACTION_MQTT_SUBSCRIBE = "mqtt subscribe";

    MqttAndroidClient mqttClient;

    public MqttService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mqttClient = new MqttAndroidClient(MqttService.this, "tcp://" + intent.getStringExtra(EXTRA_MQTT_SERVER_DOMAIN) + ":1883", MqttClient.generateClientId());
        try
        {
            mqttClient.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MqttService.this, "Connected successfully to the mqtt broker", Toast.LENGTH_LONG).show();
                    LocalBroadcastManager.getInstance(MqttService.this).registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                MqttMessage message = new MqttMessage(intent.getStringExtra(MqttService.EXTRA_MQTT_MESSAGE).getBytes());
                                mqttClient.publish(intent.getStringExtra(MqttService.EXTRA_MQTT_TOPIC), message);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new IntentFilter(ACTION_MQTT_PUBLISH));

                    try {
                        mqttClient.subscribe("topic", 1, new IMqttMessageListener() {
                            @Override
                            public void messageArrived(String topic, MqttMessage message) {
                                Intent subscriptionIntent = new Intent(MqttService.ACTION_MQTT_SUBSCRIBE);
                                subscriptionIntent.putExtra(MqttService.EXTRA_MQTT_TOPIC, topic);
                                subscriptionIntent.putExtra(MqttService.EXTRA_MQTT_MESSAGE, message.toString());
                                LocalBroadcastManager.getInstance(MqttService.this).sendBroadcast(subscriptionIntent);

                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MqttService.this)
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentTitle("new message arrived")
                                        .setContentText("message content");
                                NotificationManagerCompat.from(MqttService.this).notify(2, notificationBuilder.build());
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MqttService.this, "Failed to connect to the mqtt broker", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException exception)
        {
            Toast.makeText(MqttService.this, "MqttException occured", Toast.LENGTH_LONG).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
