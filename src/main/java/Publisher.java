import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

import java.net.URI;
import java.util.Optional;

public class Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    private QuicClientConnection connection;
    private String username;
    private String publicKey = "NONE"; // Received after Sign Up
    private static final String BROKER_PASSWORD = Optional.ofNullable(System.getenv("BROKER_PASSWORD"))
            .orElseThrow(() -> new IllegalStateException("Environment variable 'BROKER_PASSWORD' is not set"));

    public void start() {
        try {
            connection = QuicClientConnection.newBuilder()
                    .uri(URI.create("https://localhost:8443"))
                    .applicationProtocol(Alpn.PROTOCOL)
                    .noServerCertificateCheck()
                    .build();

            try {
                connection.connect();
                logger.info("Connected to the broker with secure configuration.");
            } catch (Exception e) {
                logger.error("Failed to connect securely: {}", e.getMessage());
                return;
            }

            // Simulate publishing events
            simulatePublishing();
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }

    private void simulatePublishing() {
        String[] topics = {"TRAFFIC", "WEATHER", "EMERGENCY"};
        String[] events = {"Accident on Highway 1", "Rain expected tomorrow", "Fire in downtown"};

        for (int i = 0; i < topics.length; i++) {
            logger.info("Publishing to topic '{}': {}", topics[i], events[i]);
            try {
                if (!connection.isConnected()) {
                    logger.warn("Error: Not connected to the broker.");
                    break;
                }
                MessageContent msg = new MessageContent(MessageContent.Type.PUBLISH, username, topics[i], events[i]);
                msg.setHmac(publicKey);
                send(msg);
                logger.info("Event Published Successfully.");
            } catch (Exception e) {
                logger.error("Error during event publishing: {}", e.getMessage());
            }

            // Simulate delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Simulated publishing completed.");
    }

    private void send(MessageContent msg) throws Exception {
        QuicStream s = connection.createStream(true);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
    }

    public static void main(String[] args) throws Exception {
        new Publisher().start();
    }
}