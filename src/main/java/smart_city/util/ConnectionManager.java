package smart_city.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionManager {

    public static Connection createConnection(String host, int port) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername("guest");
        factory.setPassword("guest");

        return factory.newConnection();
    }
}