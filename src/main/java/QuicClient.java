
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;
import tech.kwik.core.log.SysOutLogger;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class QuicClient {

    /**
     * @param args
     * @throws Exception
     */
    /**
     * @param args
     * @throws Exception
     */
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
    	
        // Identifier for this traffic light (client)
        String lightId =args[0];
                         // "CornicheRoad";
                       // "KhalifaCity"
                       // "YasIsland "

        // Logger setup (disable extra logs)
        SysOutLogger logger = new SysOutLogger();
        logger.logInfo(false);
        logger.logWarning(false);

        // Build QUIC client connection to the server
        QuicClientConnection connection = QuicClientConnection.newBuilder()
                .uri(URI.create("https://localhost:" + QuicServer.PORT)) // Server address
                .applicationProtocol(Alpn.PROTOCOL) // Protocol to use
                .noServerCertificateCheck() // Ignore certificate validation (for testing)
                .logger(logger)
                .build();

        // Connect to server
        System.out.println("--------------------------------------------------------------- ");
        System.out.println(lightId + " connecting to Abu Dhabi Smart Mobility Control Center...");
        System.out.println("--------------------------------------------------------------- ");
        connection.connect();

        System.out.println(lightId + " connected successfully.");
        System.out.println("--------------------------------------------------------------- ");

        // Generate random traffic data
        Random rand = new Random();

        // Random traffic light state
        String[] states = {"GREEN", "YELLOW", "RED"};
        String state = states[rand.nextInt(states.length)];

        // Random number of detected cars
        int cars = rand.nextInt(50) + 1;

        // Random emergency type
        String[] emergencies = {"AMBULANCE", "POLICE"};
        String emergency = emergencies[rand.nextInt(emergencies.length)];

        // Prepare messages to send
        String[] messages = {
                lightId + " | STATE: " + state,
                lightId + " | CARS_DETECTED: " + cars,
                lightId + " | EMERGENCY: " + emergency
        };

        // Used to wait until all messages are processed
        CountDownLatch latch = new CountDownLatch(messages.length);

        // Send each message in a separate thread (parallel streams)
        for (String message : messages) {
            Thread worker = new Thread(() -> {
                try {
                    // Create a new QUIC stream (bidirectional)
                    QuicStream stream = connection.createStream(true);

                    // Print outgoing message
                    System.out.println(lightId + " sending: " + message +
                            " | stream=" + stream.getStreamId());

                    // Simulate failure for emergency message
//                    if (message.contains("EMERGENCY")) {
//                        System.out.println("Emergency stream crashed! ...");
//                        return; // Do not send anything
//                    }

                    // Send message to server
                    MessageUtil.writeText(stream.getOutputStream(), message);

                    // Read response from server
                    String response = MessageUtil.readAll(stream.getInputStream());

                    // Print server reply
                    System.out.println(lightId + " received server reply: " + response +
                            " | stream=" + stream.getStreamId());

                } catch (Exception e) {
                    // Handle stream errors
                    System.err.println(lightId + " stream error: " + e.getMessage());
                } finally {
                    // Decrease latch count when done
                    latch.countDown();
                }
            });

            worker.start(); // Start thread
        }

        // Wait until all streams finish
        latch.await();

        System.out.println(lightId + " finished sending all traffic updates.");
        System.out.println("--------------------------------------------------------------- ");

        // We wont close the connection to demonstrate mutilple client migration
       // connection.closeAndWait();
    }
}