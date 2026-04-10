package smart_city.broker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import smart_city.util.ConnectionManager;

public class UtilitiesBroker {

    private static final String EXCHANGE = "utilities_exchange";
    private static final String HOST = "localhost";
    private static final int PORT = 5674;

    public static void main(String[] args) throws Exception {

        Connection connection = ConnectionManager.createConnection(HOST, PORT);
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE, "topic", true);

        System.out.println("⚡ Utilities Broker running on port " + PORT);
        System.out.println("Exchange: " + EXCHANGE);

        while (true) {
            Thread.sleep(1000);
        }
    }
}