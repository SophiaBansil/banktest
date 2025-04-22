package bank;

import java.io.Serializable;

public class Message implements Serializable {

    private final String id;
    private final String text;
    private final TYPE type;

    public Message(String id, String text, TYPE type) {
        this.id = id;
        this.text = text;
        this.type = type;
    }

    public String getID() {
        return id;
    }

    public String getText() {
        return text;
    }

    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", text='" + text + '\'' +
                '}';
    }
}

