import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

import java.net.URI;
import java.util.Scanner;

public class Publisher {
    private QuicClientConnection connection;
    private String username;
    private String privateKey = "SECRET_TOKEN";

    public void start() throws Exception {
        Scanner sc = new Scanner(System.in);
        connection = QuicClientConnection.newBuilder()
                .uri(URI.create("https://localhost:4433"))
                .applicationProtocol(Alpn.PROTOCOL)
                .noServerCertificateCheck()
                .build();
        connection.connect();

        while (true) {
            System.out.println("\n--- SmartFlow Publisher ---");
            System.out.println("1. Sign Up (Get Public Key)");
            System.out.println("2. Publish Event");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int choice = sc.nextInt(); sc.nextLine();

            if (choice == 1) {
                System.out.print("Username: ");
                username = sc.nextLine();
                MessageContent msg = new MessageContent(MessageContent.Type.SIGNUP, username, "SYS", privateKey);
                send(msg);
                System.out.println("Registered successfully.");
            } else if (choice == 2) {
                if (username == null) { System.out.println("Sign up first!"); continue; }
                System.out.print("Topic (TRAFFIC/WEATHER): ");
                String topic = sc.nextLine().toUpperCase();
                System.out.print("Message: ");
                String payload = sc.nextLine();
                
                MessageContent msg = new MessageContent(MessageContent.Type.PUBLISH, username, topic, payload);
                msg.setHmac(privateKey); // Legitimacy check
                send(msg);
            } else { break; }
        }
    }

    private void send(MessageContent msg) throws Exception {
        QuicStream s = connection.createStream(true);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
        System.out.println("Server ACK: " + MessageUtill.readAll(s.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        new Publisher().start();
    }
}