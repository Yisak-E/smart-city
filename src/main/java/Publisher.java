import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import smart_city.Alpn;
import smart_city.MessageUtill;
import supporters.MessageContent;

import java.net.URI;
import java.util.Optional;
import java.util.Scanner;

public class Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    private QuicClientConnection connection;
    private String username;
    private String publicKey = "NONE"; // Received after Sign Up
    private static final String BROKER_PASSWORD = Optional.ofNullable(System.getenv("BROKER_PASSWORD"))
            .orElseThrow(() -> new IllegalStateException("Environment variable 'BROKER_PASSWORD' is not set"));

    public void start() {
        Scanner sc = new Scanner(System.in);
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

            while (true) {
                logger.info("\n--- SmartFlow City Service Menu ---");
                logger.info("1. Sign Up (Register Service)");
                logger.info("2. Log In");
                logger.info("3. Publish City Event");
                logger.info("4. Exit");
                logger.info("Choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid input. Please enter a number between 1 and 4.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        logger.info("Enter Service Name: ");
                        username = sc.nextLine().trim();
                        if (username.isEmpty()) {
                            logger.warn("Error: Service name cannot be empty.");
                            break;
                        }
                        logger.info("Set Password: ");
                        String pass = sc.nextLine();
                        if (pass.isEmpty()) {
                            logger.warn("Error: Password cannot be empty.");
                            break;
                        }
                        try {
                            if (!connection.isConnected()) {
                                logger.warn("Error: Not connected to the broker.");
                                break;
                            }
                            String resp = request(MessageContent.Type.SIGNUP, username, "AUTH", pass);
                            if (resp.contains("KEY_")) {
                                publicKey = resp.split(":")[1].trim();
                                logger.info("Signed up successfully! Key: {}", publicKey);
                            } else if (resp.contains("ACK|Key Registered")) {
                                logger.warn("Sign-up failed: Service name '{}' is already registered.", username);
                            } else {
                                logger.warn("Sign-up failed: {}", resp);
                            }
                        } catch (Exception e) {
                            logger.error("Error during sign-up: {}", e.getMessage());
                        }
                        break;

                    case 2:
                        logger.info("Username: ");
                        username = sc.nextLine().trim();
                        if (username.isEmpty()) {
                            logger.warn("Error: Username cannot be empty.");
                            break;
                        }
                        logger.info("Password: ");
                        String lPass = sc.nextLine();
                        try {
                            if (!connection.isConnected()) {
                                logger.warn("Error: Not connected to the broker.");
                                break;
                            }
                            String status = request(MessageContent.Type.LOGIN, username, "AUTH", lPass);
                            logger.info("Login Status: {}", status);
                        } catch (Exception e) {
                            logger.error("Error during login: {}", e.getMessage());
                        }
                        break;

                    case 3:
                        if (username == null) {
                            logger.warn("Error: Please log in first.");
                            break;
                        }
                        logger.info("Topic (e.g., TRAFFIC.accidents): ");
                        String topic = sc.nextLine().toUpperCase().trim();
                        if (topic.isEmpty()) {
                            logger.warn("Error: Topic cannot be empty.");
                            break;
                        }
                        logger.info("Enter Event Details: ");
                        String event = sc.nextLine().trim();
                        if (event.isEmpty()) {
                            logger.warn("Error: Event details cannot be empty.");
                            break;
                        }
                        try {
                            if (!connection.isConnected()) {
                                logger.warn("Error: Not connected to the broker.");
                                break;
                            }
                            MessageContent msg = new MessageContent(MessageContent.Type.PUBLISH, username, topic, event);
                            msg.setHmac(publicKey);
                            send(msg);
                            logger.info("Event Published Successfully.");
                        } catch (Exception e) {
                            logger.error("Error during event publishing: {}", e.getMessage());
                        }
                        break;

                    case 4:
                        logger.info("Exiting...");
                        return;

                    default:
                        logger.warn("Invalid choice. Please select a valid option.");
                }
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
            sc.close();
        }
    }

    private String request(MessageContent.Type type, String user, String topic, String payload) throws Exception {
        QuicStream s = connection.createStream(true);
        MessageContent msg = new MessageContent(type, user, topic, payload);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
        return MessageUtill.readAll(s.getInputStream());
    }

    private void send(MessageContent msg) throws Exception {
        QuicStream s = connection.createStream(true);
        MessageUtill.writeText(s.getOutputStream(), msg.encode());
    }

    public static void main(String[] args) throws Exception {
        new Publisher().start();
    }
}