package smart_city.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BrokerMetrics {

    private static final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public static void increment(String broker) {
        counters.putIfAbsent(broker, new AtomicInteger(0));
        counters.get(broker).incrementAndGet();
    }

    public static void printStats() {
        System.out.println("\n📊 Broker Utilization:");
        counters.forEach((broker, count) ->
                System.out.println(broker + " → " + count.get() + " messages"));
    }
}