package smart_city.broker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import smart_city.util.ConnectionManager;

public class TrafficBroker {

    private static final String EXCHANGE = "traffic_exchange";
    private static final String HOST = "localhost";
    private static final int PORT = 5672;

    public static void main(String[] args) throws Exception {

        Connection connection = ConnectionManager.createConnection(HOST, PORT);
        Channel channel = connection.createChannel();

        // Declare exchange
        channel.exchangeDeclare(EXCHANGE, "topic", true);

        System.out.println("🚦 Traffic Broker running on port " + PORT);
        System.out.println("Exchange: " + EXCHANGE);

        // Keep alive
        while (true) {
            Thread.sleep(1000);
        }
    }
}