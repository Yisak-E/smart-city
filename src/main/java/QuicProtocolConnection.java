import tech.kwik.core.QuicConnection;
import tech.kwik.core.QuicStream;
import tech.kwik.core.server.ApplicationProtocolConnection;
import smart_city.MessageUtill;
import supporters.MessageContent;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuicProtocolConnection implements ApplicationProtocolConnection {
    private static final Logger logger = LoggerFactory.getLogger(QuicProtocolConnection.class);
    private final QuicConnection quicConnection;
    private final Map<String, List<QuicProtocolConnection>> registry;
    private String clientPublicKey;

    public QuicProtocolConnection(QuicConnection conn, Map<String, List<QuicProtocolConnection>> registry) {
        this.quicConnection = conn;
        this.registry = registry;
    }

    @Override
    public void acceptPeerInitiatedStream(QuicStream stream) {
        new Thread(() -> {
            try {
                String raw = MessageUtill.readAll(stream.getInputStream());
                MessageContent msg = MessageContent.decode(raw);

                if (msg.getType() == MessageContent.Type.SIGNUP) {
                    this.clientPublicKey = msg.getPayload();
                    MessageUtill.writeText(stream.getOutputStream(), "ACK|Key Registered");
                    logger.info("Client signed up with public key.");
                }
                else if (msg.getType() == MessageContent.Type.SUBSCRIBE) {
                    registry.computeIfAbsent(msg.getTopic(), k -> new CopyOnWriteArrayList<>()).add(this);
                    logger.info("[BROKER] Client subscribed to topic: {}", msg.getTopic());
                }
                else if (msg.getType() == MessageContent.Type.PUBLISH) {
                    broadcast(msg);
                    MessageUtill.writeText(stream.getOutputStream(), "ACK|Event Distributed");
                    logger.info("[BROKER] Event published to topic: {}", msg.getTopic());
                }
            } catch (Exception e) {
                logger.error("Error processing stream: {}", e.getMessage());
            }
        }).start();
    }

    private void broadcast(MessageContent msg) {
        List<QuicProtocolConnection> subs = registry.get(msg.getTopic());
        if (subs != null) {
            for (QuicProtocolConnection sub : subs) {
                try {
                    sub.deliverSecure(msg);
                    logger.info("[BROKER] Delivered message to subscriber.");
                } catch (Exception e) {
                    logger.error("Failed to deliver message to subscriber: {}", e.getMessage());
                }
            }
        } else {
            logger.warn("No subscribers for topic: {}", msg.getTopic());
        }
    }

    public void deliverSecure(MessageContent msg) {
        try {
            String data = msg.encode() + (clientPublicKey != null ? clientPublicKey : "");
            String encrypted = Base64.getEncoder().encodeToString(data.getBytes());

            QuicStream pushStream = quicConnection.createStream(true);
            MessageUtill.writeText(pushStream.getOutputStream(), encrypted);
            logger.info("[BROKER] Secure message delivered.");
        } catch (Exception e) {
            logger.error("Error delivering secure message: {}", e.getMessage());
        }
    }
}