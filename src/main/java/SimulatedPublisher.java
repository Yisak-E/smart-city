import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

public class SimulatedPublisher {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedPublisher.class);
    private QuicClientConnection connection;
    private static final String PUBLIC_KEY = "NONE"; // Received after Sign Up

    public void start() {
        try {
            connectToBroker();

            // Load events from CSV and publish
            loadEventsFromCSV();

            // Keep listening for further events
            while (true) {
                Thread.sleep(1000); // Simulate waiting for new events
            }
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

    private void connectToBroker() {
        try {
            connection = QuicClientConnection.newBuilder()
                    .uri(URI.create("https://localhost:8443"))
                    .applicationProtocol(Alpn.PROTOCOL)
                    .noServerCertificateCheck()
                    .build();

            connection.connect();
            logger.info("Connected to the broker with secure configuration.");
        } catch (Exception e) {
            logger.error("Failed to connect securely: {}", e.getMessage());
        }
    }

    private void loadEventsFromCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/events.csv"))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String topic = parts[0];
                String event = parts[1];
                publishEvent(topic, event);
            }
        } catch (IOException e) {
            logger.error("Error reading events from CSV: {}", e.getMessage());
        }
    }

    private void publishEvent(String topic, String event) {
        logger.info("Publishing to topic '{}': {}", topic, event);
        try {
            if (!connection.isConnected()) {
                logger.warn("Error: Not connected to the broker.");
                return;
            }
            MessageContent msg = new MessageContent(MessageContent.Type.PUBLISH, "SimulatedPublisher", topic, event);
            msg.setHmac(PUBLIC_KEY);
            send(msg);
            logger.info("Event Published Successfully.");
        } catch (Exception e) {
            logger.error("Error during event publishing: {}", e.getMessage());
        }
    }

    private void send(MessageContent msg) throws IOException {
        QuicStream s = connection.createStream(true);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
    }

    public static void main(String[] args) {
        new SimulatedPublisher().start();
    }
}
