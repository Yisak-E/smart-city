package smart_city.config;

import smart_city.config.BrokerConfig.BrokerInfo;

import java.util.HashMap;
import java.util.Map;

public class RoutingConfigLoader {

    public static Map<String, BrokerInfo> load() {

        Map<String, BrokerInfo> map = new HashMap<>();

        map.put("TRAFFIC", new BrokerInfo("localhost", 5672, "traffic_exchange"));
        map.put("WEATHER", new BrokerInfo("localhost", 5673, "weather_exchange"));
        map.put("WATER", new BrokerInfo("localhost", 5674, "utilities_exchange"));
        map.put("ELECTRICITY", new BrokerInfo("localhost", 5674, "utilities_exchange"));

        return map;
    }
}