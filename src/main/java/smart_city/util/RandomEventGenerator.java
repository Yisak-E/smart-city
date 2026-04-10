package smart_city.util;

import java.util.Random;

public class RandomEventGenerator {

    private static final String[] TOPICS = {
            "TRAFFIC.accident", "TRAFFIC.congestion",
            "WEATHER.rain", "WEATHER.storm",
            "EMERGENCY.fire", "EMERGENCY.police"
    };

    private static final String[] MESSAGES = {
            "Accident reported",
            "Heavy traffic",
            "Rain expected",
            "Storm warning",
            "Fire alert",
            "Police activity"
    };

    public static String[] generate() {
        Random r = new Random();

        return new String[]{
                TOPICS[r.nextInt(TOPICS.length)],
                MESSAGES[r.nextInt(MESSAGES.length)]
        };
    }
}