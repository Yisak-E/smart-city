package smart_city.routing;

import smart_city.config.BrokerConfig.BrokerInfo;

import java.util.HashMap;
import java.util.Map;

public class TopicRoutingService {

    private final Map<String, BrokerInfo> routingTable = new HashMap<>();

    // 🔥 dynamic configuration (NO HARDCODING in logic)
    public TopicRoutingService(Map<String, BrokerInfo> config) {
        this.routingTable.putAll(config);
    }

    public BrokerInfo resolve(String topic) {

        for (String key : routingTable.keySet()) {
            if (topic.startsWith(key)) {
                return routingTable.get(key);
            }
        }

        throw new RuntimeException("No broker found for topic: " + topic);
    }
}