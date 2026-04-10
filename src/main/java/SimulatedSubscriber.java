import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kwik.core.QuicClientConnection;
import java.net.URI;
import smart_city.Alpn;

public class SimulatedSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedSubscriber.class);
    private QuicClientConnection connection;

    public void start() {
        try {
            connectToBroker();

            // Actively listen for events
            listenForEvents();
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

    private void listenForEvents() {
        try {
            while (true) {
                // Placeholder for handling incoming streams
                logger.info("Listening for events...");
                Thread.sleep(1000); // Simulate waiting for events
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        new SimulatedSubscriber().start();
    }
}
