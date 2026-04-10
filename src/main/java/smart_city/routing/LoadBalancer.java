package smart_city.routing;

import smart_city.config.BrokerConfig.BrokerInfo;

public class LoadBalancer {

    private final TopicRoutingService routingService;

    public LoadBalancer(TopicRoutingService routingService) {
        this.routingService = routingService;
    }

    public BrokerInfo getBroker(String topic) {
        return routingService.resolve(topic);
    }
}