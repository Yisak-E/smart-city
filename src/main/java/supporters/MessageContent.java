package supporters;

import java.io.Serializable;

public class MessageContent implements Serializable {
    public enum Type { SIGNUP, LOGIN, PUBLISH, SUBSCRIBE, ACK, ERROR }

    private Type type;
    private String sender;    
    private String topic;     
    private String payload;   
    private String hmac;      
    private long timestamp;   

    public MessageContent(Type type, String sender, String topic, String payload) {
        this.type = type;
        this.sender = sender;
        this.topic = topic;
        this.payload = payload;
        this.timestamp = System.nanoTime();
    }

    // Serialization: TYPE|SENDER|TOPIC|PAYLOAD|TIMESTAMP|HMAC
    public String encode() {
        return type + "|" + sender + "|" + topic + "|" + payload + "|" + timestamp + "|" + (hmac == null ? "NONE" : hmac);
    }

    public static MessageContent decode(String data) {
        String[] p = data.split("\\|", 6);
        MessageContent msg = new MessageContent(Type.valueOf(p[0]), p[1], p[2], p[3]);
        msg.timestamp = Long.parseLong(p[4]);
        msg.hmac = p[5];
        return msg;
    }

    // Getters and Setters
    public Type getType() { return type; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public String getSender() { return sender; }
    public long getTimestamp() { return timestamp; }
    public String getHmac() { return hmac; }
    public void setHmac(String hmac) { this.hmac = hmac; }
}