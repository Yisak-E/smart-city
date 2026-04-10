import tech.kwik.core.QuicConnection;
import tech.kwik.core.server.ApplicationProtocolConnection;
import tech.kwik.core.server.ApplicationProtocolConnectionFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuicProtocolFactory implements ApplicationProtocolConnectionFactory {
    
    // Topic Registry: Maps a Topic Name (e.g., "TRAFFIC.accidents") to a list of active Subscriber Connections.
    // Uses ConcurrentHashMap and CopyOnWriteArrayList for thread-safety during high-frequency events.
    private final Map<String, List<QuicProtocolConnection>> topicRegistry = new ConcurrentHashMap<>();

    @Override
    public ApplicationProtocolConnection createConnection(String protocol, QuicConnection quicConnection) {
        // Every time a new client (Publisher or Subscriber) connects, we create a new handler
        // and share the topicRegistry so they can communicate.
        return new QuicProtocolConnection(quicConnection, topicRegistry);
    }

    @Override
    public int maxConcurrentPeerInitiatedBidirectionalStreams() {
        // High limit to support many concurrent updates and parallel push-notifications
        return 100;
    }

    @Override
    public int maxConcurrentPeerInitiatedUnidirectionalStreams() {
        // SmartFlow uses bidirectional streams for Request-Response and Push updates
        return 0;
    }
}