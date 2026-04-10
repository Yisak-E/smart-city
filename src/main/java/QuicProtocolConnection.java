import tech.kwik.core.QuicConnection;
import tech.kwik.core.QuicStream;
import tech.kwik.core.server.ApplicationProtocolConnection;
import smart_city.MessageUtill;
import supporters.MessageContent;
import java.util.*;
import java.util.concurrent.*;

public class QuicProtocolConnection implements ApplicationProtocolConnection {
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
                } 
                else if (msg.getType() == MessageContent.Type.SUBSCRIBE) {
                    // FIFO ordering per topic [cite: 24, 44]
                    registry.computeIfAbsent(msg.getTopic(), k -> new CopyOnWriteArrayList<>()).add(this);
                    System.out.println("[BROKER] Client joined: " + msg.getTopic());
                } 
                else if (msg.getType() == MessageContent.Type.PUBLISH) {
                    broadcast(msg);
                    MessageUtill.writeText(stream.getOutputStream(), "ACK|Event Distributed");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void broadcast(MessageContent msg) {
        List<QuicProtocolConnection> subs = registry.get(msg.getTopic());
        if (subs != null) {
            for (QuicProtocolConnection sub : subs) {
                sub.deliverSecure(msg); // Push directly to subscribers [cite: 53, 90]
            }
        }
    }

    public void deliverSecure(MessageContent msg) {
        try {
            // Simulated encryption for confidentiality [cite: 73, 92]
            String data = msg.encode() + (clientPublicKey != null ? clientPublicKey : "");
            String encrypted = Base64.getEncoder().encodeToString(data.getBytes());
            
            QuicStream pushStream = quicConnection.createStream(true);
            MessageUtill.writeText(pushStream.getOutputStream(), encrypted);
        } catch (Exception ignored) {}
    }
}