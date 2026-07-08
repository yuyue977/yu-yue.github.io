package com.example.xiamenbackground.mqtt;

import com.example.xiamenbackground.service.DataPushService;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MqttService implements CommandLineRunner {

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private DataPushService dataPushService;

    @Override
    public void run(String... args) throws Exception {
        // 设置回调
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void disconnected(org.eclipse.paho.mqttv5.client.MqttDisconnectResponse disconnectResponse) {
                System.out.println("=== MQTT 断开连接 ===");
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                System.out.println("=== MQTT 错误: " + exception.getMessage() + " ===");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("=== MQTT 收到消息, topic: " + topic + " ===");

                // 判断话题类型，执行不同逻辑
                if (topic.endsWith("/data")) {
                    // 收到 /xmLoader/data - 料仓/料斗实时数据
                    System.out.println("=== 收到料仓/料斗数据，查询数据库并推送 ===");
                    dataPushService.pushLatestData();

                } else if (topic.endsWith("/refresh")) {
                    // 收到 /web/refresh - Unity 通知刷新
                    System.out.println("=== 收到 Unity 刷新通知，重新查询数据库 ===");
                    dataPushService.pushLatestData();
                }
            }

            @Override
            public void deliveryComplete(org.eclipse.paho.mqttv5.client.IMqttToken token) {
                // 发送完成
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("=== MQTT 连接完成, reconnect=" + reconnect + " ===");
                if (reconnect) {
                    try {
                        subscribe();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
                // 认证包
            }
        });

        // 订阅话题
        subscribe();
    }

    private void subscribe() throws Exception {
        // 订阅料仓/料斗数据话题（可选，用于直接接收设备数据）
        MqttSubscription dataSubscription = new MqttSubscription("/xmLoader/data", 1);

        // 订阅刷新通知话题（主要用途，接收 Unity 的刷新指令）
        MqttSubscription refreshSubscription = new MqttSubscription("/xmLoader/web/refresh", 1);

        mqttClient.subscribe(new MqttSubscription[]{dataSubscription, refreshSubscription});
        System.out.println("=== MQTT 已订阅话题: /xmLoader/data, /xmLoader/web/refresh ===");
    }

    /**
     * 发布 MQTT 消息
     * @param topic 话题
     * @param payload 消息内容
     */
    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            System.out.println("=== MQTT 已发布消息, topic: " + topic + " ===");
        } catch (Exception e) {
            System.err.println("=== MQTT 发布失败: " + e.getMessage() + " ===");
        }
    }
}
