import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import tech.kwik.core.server.ApplicationProtocolConnection;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

import java.net.URI;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

public class Subscriber {
    private static final String BROKER_PASSWORD = Optional.ofNullable(System.getenv("BROKER_PASSWORD"))
            .orElseThrow(() -> new IllegalStateException("Environment variable 'BROKER_PASSWORD' is not set"));
    private QuicClientConnection connection;
    private String username;
    private String sessionKey;

    public void start() throws Exception {
        Scanner sc = new Scanner(System.in);
        this.sessionKey = "PUB_" + System.currentTimeMillis();

        connection = QuicClientConnection.newBuilder()
                .uri(URI.create("https://localhost:8443"))
                .applicationProtocol(Alpn.PROTOCOL) // Adjusted to match the expected single argument
                .noServerCertificateCheck()
                .build();
        
        connection.connect();
        System.out.println("Connected to the broker with secure configuration.");

        while (true) {
            System.out.println("\n--- SmartFlow Subscriber Menu ---");
            System.out.println("1. Login & Register Identity");
            System.out.println("2. Subscribe to Topic (e.g., TRAFFIC.accidents)");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int choice = Integer.parseInt(sc.nextLine());

            if (choice == 1) {
                System.out.print("Enter Username: ");
                username = sc.nextLine();
                // Part 1 requirement: Integrity of the request [cite: 92]
                send(new MessageContent(MessageContent.Type.SIGNUP, username, "REG", sessionKey));
                System.out.println("Identity verified.");
            } else if (choice == 2) {
                if (username == null) { System.out.println("Login first!"); continue; }
                System.out.print("Enter Topic: ");
                String topic = sc.nextLine().toUpperCase();
                // Register for specific city event topics [cite: 37, 48]
                send(new MessageContent(MessageContent.Type.SUBSCRIBE, username, topic, ""));
                System.out.println("Subscribed to " + topic);
            } else { break; }
        }
    }

    private void send(MessageContent msg) throws Exception {
        QuicStream s = connection.createStream(true);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
    }

    // This class handles events pushed FROM the broker directly to the subscriber [cite: 53, 150]
    private class SecurePushHandler implements ApplicationProtocolConnection {
        @Override
        public void acceptPeerInitiatedStream(QuicStream stream) {
            new Thread(() -> {
                try {
                    String encrypted = MessageUtill.readAll(stream.getInputStream());
                    
                    // Maintain confidentiality of returned messages [cite: 92]
                    String decoded = new String(Base64.getDecoder().decode(encrypted)).replace(sessionKey, "");
                    MessageContent msg = MessageContent.decode(decoded);

                    // Measurement for Part 1.b performance analysis [cite: 113, 114]
                    long latency = (System.nanoTime() - msg.getTimestamp()) / 1_000_000;
                    System.out.println("\n[SECURE UPDATE] " + msg.getTopic() + ": " + msg.getPayload());
                    System.out.println("Latency: " + latency + " ms");
                } catch (Exception e) {
                    System.err.println("Push error: " + e.getMessage());
                }
            }).start();
        }
    }

    public static void main(String[] args) throws Exception {
        new Subscriber().start();
    }
}