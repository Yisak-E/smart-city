package smart_city.config;

public class BrokerConfig {

    public static class BrokerInfo {
        public String host;
        public int port;
        public String exchange;

        public BrokerInfo(String host, int port, String exchange) {
            this.host = host;
            this.port = port;
            this.exchange = exchange;
        }
    }
}