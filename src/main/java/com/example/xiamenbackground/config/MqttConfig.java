package com.example.xiamenbackground.config;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttConfig {

    @Bean
    public MqttClient mqttClient() {
        String broker = "tcp://81.70.135.119:1883";
        String clientId = "web-" + UUID.randomUUID().toString().substring(0, 8);
        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);
            options.setCleanStart(true);

            client.connect(options);
            System.out.println("=== MQTT 已连接到 Broker: " + broker + " ===");

            return client;
        } catch (Exception e) {
            System.err.println("=== MQTT 连接失败，应用将继续启动: " + e.getMessage() + " ===");
            // 返回一个未连接的客户端，后续可通过自动重连恢复
            try {
                MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
                return client;
            } catch (Exception ex) {
                throw new RuntimeException("无法创建 MQTT 客户端", ex);
            }
        }
    }
}
