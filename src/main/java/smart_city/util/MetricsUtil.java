package smart_city.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class MetricsUtil {

    private static final String FILE = "metrics.csv";

    // ================= LOG LATENCY =================
    public static synchronized void logLatency(
            String topic,
            String broker,
            long latency
    ) {

        try (FileWriter writer = new FileWriter(FILE, true)) {

            writer.append(LocalDateTime.now().toString())
                    .append(",")
                    .append(topic)
                    .append(",")
                    .append(broker)
                    .append(",")
                    .append(String.valueOf(latency))
                    .append("\n");

        } catch (IOException e) {
            System.out.println("❌ Metrics write failed");
        }
    }
}