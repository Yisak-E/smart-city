import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import java.net.URI;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

public class SimulatedSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedSubscriber.class);
    private QuicClientConnection connection;

    public void connectToBroker() {
        try {
            connection = QuicClientConnection.newBuilder()
                    .uri(URI.create("https://localhost:8443"))
                    .applicationProtocol(Alpn.PROTOCOL)
                    .noServerCertificateCheck()
                    .build();
            connection.connect();
            logger.info("Connected to the broker with secure configuration.");
        } catch (Exception e) {
            logger.error("Failed to connect to the broker: {}", e.getMessage());
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            QuicStream stream = connection.createStream(true);
            MessageContent subscribeMsg = new MessageContent(MessageContent.Type.SUBSCRIBE, topic, "", "");
            MessageUtill.writeText(stream.getOutputStream(), subscribeMsg.encode());
            logger.info("Subscribing to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Failed to subscribe to topic {}: {}", topic, e.getMessage());
        }
    }

    public static void main(String[] args) {
        SimulatedSubscriber subscriber = new SimulatedSubscriber();
        subscriber.connectToBroker();
        subscriber.subscribeToTopic("TRAFFIC");
    }
}
