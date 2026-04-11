package smart_city.publisher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import smart_city.config.BrokerConfig.BrokerInfo;
import smart_city.config.RoutingConfigLoader;
import smart_city.routing.LoadBalancer;
import smart_city.routing.TopicRoutingService;
import smart_city.util.BrokerMetrics;
import smart_city.util.ConnectionManager;
import smart_city.util.SecurityUtil;

import java.util.HashMap;
import java.util.Map;

public class Publisher {

    private final LoadBalancer loadBalancer;

    // cache connections per broker (IMPORTANT for performance)
    private final Map<String, Connection> connections = new HashMap<>();
    private final Map<String, Channel> channels = new HashMap<>();

    public Publisher() {
        TopicRoutingService routingService =
                new TopicRoutingService(RoutingConfigLoader.load());

        this.loadBalancer = new LoadBalancer(routingService);
    }

    // ================= GET CHANNEL PER BROKER =================
    private Channel getChannel(BrokerInfo broker) throws Exception {

        String key = broker.host + ":" + broker.port;

        if (channels.containsKey(key)) {
            return channels.get(key);
        }

        Connection conn = ConnectionManager.createConnection(
                broker.host,
                broker.port
        );

        Channel channel = conn.createChannel();

        // declare exchange safely
        channel.exchangeDeclare(broker.exchange, "topic", true);

        connections.put(key, conn);
        channels.put(key, channel);

        return channel;
    }

    // ================= PUBLISH EVENT =================
    public void publishEvent(String topic, String message) throws Exception {

        BrokerInfo broker = loadBalancer.getBroker(topic);

        Channel channel = getChannel(broker);

        long timestamp = System.currentTimeMillis();

        // payload
        String payload = message + "|" + timestamp;

        // 🔐 HMAC (integrity)
        String hmac = SecurityUtil.generateHmac(payload);

        // 🔒 AES encryption (confidentiality)
        String encryptedPayload = SecurityUtil.encrypt(payload);

        String finalMessage = encryptedPayload + "|" + hmac;

        channel.basicPublish(
                broker.exchange,
                topic,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                finalMessage.getBytes()
        );

        System.out.println("📤 [" + broker.exchange + "] "
                + topic + " → " + message);
        BrokerMetrics.increment(broker.exchange);
    }

    // ================= LOAD TEST =================
    public void loadTest(int count) throws Exception {

        String[] topics = {
                "TRAFFIC.accident",
                "TRAFFIC.congestion",
                "WEATHER.storm",
                "WEATHER.rain",
                "WATER.leak",
                "ELECTRICITY.outage"
        };

        String[] messages = {
                "Accident reported",
                "Heavy traffic",
                "Storm warning",
                "Rain expected",
                "Water pipe leak",
                "Power outage"
        };

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {

            int idx = i % topics.length;

            publishEvent(
                    topics[idx],
                    messages[idx] + " #" + i
            );

            Thread.sleep(5); // simulate event rate
        }

        long end = System.currentTimeMillis();

        System.out.println("⏱ Sent " + count + " events in "
                + (end - start) + " ms");
    }

    // ================= CLOSE CONNECTIONS =================
    public void close() {
        connections.values().forEach(conn -> {
            try { conn.close(); } catch (Exception ignored) {}
        });
    }

    // ================= MAIN =================
    public static void main(String[] args) throws Exception {

        Publisher publisher = new Publisher();

        // 🔥 HIGH LOAD SIMULATION (adjust as needed)
        publisher.loadTest(2000);
        BrokerMetrics.printStats();
        publisher.close();
    }
}