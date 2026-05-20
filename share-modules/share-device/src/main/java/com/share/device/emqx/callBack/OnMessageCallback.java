package com.share.device.emqx.callBack;

import com.alibaba.fastjson.JSONObject;
import com.share.device.emqx.config.EmqxProperties;
import com.share.device.emqx.factory.MessageHandlerFactory;
import com.share.device.emqx.handler.MassageHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OnMessageCallback implements MqttCallback {

    @Resource
    private EmqxProperties emInfo;
    @Resource
    private MessageHandlerFactory messageHandlerFactory;

    private MqttClient client;
    private String brokerUrl;
    private String clientId;
    private String userName;
    private String password;
    private int maxReconnectAttempts; // 最大重试次数
    private int reconnectDelay;    // 初始重连延迟（毫秒）
    private int currentAttempts = 0;      // 当前重试次数
    
    @jakarta.annotation.PostConstruct
    private void init() {
        // 在依赖注入完成后初始化属性
        this.brokerUrl = emInfo.getServerURI();
        this.clientId = emInfo.getClientId();
        this.userName = emInfo.getUsername();
        this.password = emInfo.getPassword();
        this.maxReconnectAttempts = emInfo.getMaxReconnectAttempts();
        this.reconnectDelay = emInfo.getReconnectDelay();
    }


    @Override
    public void connectionLost(Throwable cause) {

        // 连接丢失后，进行重连
        System.out.println("连接断开，原因: " + cause.getMessage());
        System.out.println("尝试自动重连...");

        // 重置重连状态（每次触发connectionLost时重置计数器）
        currentAttempts = 0;

        while (currentAttempts < maxReconnectAttempts) {
            try {
                // 1. 检查旧的client是否可重用
                if (client != null && !client.isConnected()) {
                    client.disconnectForcibly(); // 强制清理旧连接
                }

                // 2. 创建全新的client实例（避免旧连接状态污染）
                MqttClient newClient = new MqttClient(
                        brokerUrl,
                        clientId + "_reconnect_" + System.currentTimeMillis(), // 避免ClientId冲突
                        new MemoryPersistence()
                );

                // 3. 配置连接选项
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setUserName(userName);
                options.setPassword(password.toCharArray());
                options.setAutomaticReconnect(false);

                // 4. 尝试连接
                System.out.println("第 " + (currentAttempts + 1) + " 次重连...");
                newClient.connect(options);

                // 5. 连接成功后替换旧client并设置回调
                this.client = newClient;
                this.client.setCallback(this); // 重新注册回调
                System.out.println("重连成功！");
                return;

            } catch (Exception e) {
                currentAttempts++;
                reconnectDelay = Math.min(reconnectDelay * 2, 30000); // 上限30秒
                System.err.println("重连失败: " + e.getMessage());

                try {
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.err.println("已达到最大重试次数(" + maxReconnectAttempts + ")，停止重连");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // subscribe后得到的消息会执行到这里面
        System.out.println("接收消息主题:" + topic);
        System.out.println("接收消息Qos:" + message.getQos());
        System.out.println("接收消息内容:" + new String(message.getPayload()));
        try {
            // 根据主题选择不同的处理逻辑
            MassageHandler massageHandler = messageHandlerFactory.getMassageHandler(topic);
            if(massageHandler!=null) {
                String content = new String(message.getPayload());
                // 直接解析消息内容为JSON对象
                JSONObject jsonMessage = JSONObject.parseObject(content);
                massageHandler.handleMessage(jsonMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("mqtt消息异常：{}", new String(message.getPayload()));
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
