package module;

public class Event {
    private String event_type;
    private String data;
    private Integer timestamp;

    public String getEvent_type() {
        return event_type;
    }

    public String getData() {
        return data;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Event{" +
                "event_type='" + event_type + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
