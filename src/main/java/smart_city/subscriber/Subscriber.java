package smart_city.subscriber;

import com.rabbitmq.client.*;

import smart_city.config.BrokerConfig.BrokerInfo;
import smart_city.config.RoutingConfigLoader;
import smart_city.routing.TopicRoutingService;
import smart_city.util.ConnectionManager;
import smart_city.util.SecurityUtil;
import smart_city.util.MetricsUtil;

public class Subscriber {

    private Connection connection;
    private Channel channel;

    private final TopicRoutingService routingService;

    public Subscriber() {
        this.routingService =
                new TopicRoutingService(RoutingConfigLoader.load());
    }

    // ================= CONNECT =================
    public void connectToBroker(String topic) throws Exception {

        BrokerInfo broker = routingService.resolve(topic);

        connection = ConnectionManager.createConnection(
                broker.host,
                broker.port
        );

        channel = connection.createChannel();

        channel.exchangeDeclare(broker.exchange, "topic", true);

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, broker.exchange, topic);

        System.out.println("✅ Connected to " + broker.exchange +
                " for topic: " + topic);

        listenForEvents(queueName);
    }

    // ================= LISTEN =================
    private void listenForEvents(String queueName) throws Exception {

        DeliverCallback callback = (consumerTag, delivery) -> {

            try {
                String received = new String(delivery.getBody());

                String[] parts = received.split("\\|");

                String encryptedPayload = parts[0];
                String hmac = parts[1];

                // 🔓 Decrypt
                String payload = SecurityUtil.decrypt(encryptedPayload);

                String[] data = payload.split("\\|");
                String message = data[0];
                long timestamp = Long.parseLong(data[1]);

                // 🔐 Verify HMAC
                if (!SecurityUtil.verifyHmac(payload, hmac)) {
                    System.out.println("❌ Invalid message");
                    return;
                }

                // 🐢 Simulated processing delay
                //Thread.sleep(50);

                long latency = System.currentTimeMillis() - timestamp;

                // 📊 Log latency
                MetricsUtil.logLatency(
                        delivery.getEnvelope().getRoutingKey(),   // topic
                        channel.getConnection().getAddress().toString(), // broker
                        latency
                );

                System.out.println("📥 " + message +
                        " | Latency: " + latency + " ms");

            } catch (Exception e) {
                System.out.println("❌ Error processing message");
            }
        };

        channel.basicConsume(queueName, true, callback, consumerTag -> {});
    }

    // ================= MAIN =================
    public static void main(String[] args) throws Exception {

        Subscriber subscriber = new Subscriber();

        // 🔥 change topic for testing
        //subscriber.connectToBroker("TRAFFIC.*");
        subscriber.connectToBroker("WEATHER.*");
        //subscriber.connectToBroker("ELECTRICITY.*");



        System.out.println("📡 Waiting for messages...");
    }
}