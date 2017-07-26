package nl.johannisk.node.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message implements Comparable{
    final int index;
    final String text;

    public Message(@JsonProperty("index") int index, @JsonProperty("text") String text) {
        this.index = index;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return index == message.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return "Message{" +
                "index=" + index +
                ", text='" + text + '\'' +
                '}';
    }


    @Override
    public int compareTo(Object o) {
        if(o instanceof Message) {
            Message m = (Message)o;
            return this.getIndex() - m.getIndex();
        }
        return 0;
    }
}
