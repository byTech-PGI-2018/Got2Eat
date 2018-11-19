package bytech.got2eat;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

public class Message implements IMessage {
    private String text;
    private String id;
    private Date createdAt;
    private Author author;

    public Message(String text, String id, Date createdAt, Author author) {
        this.text = text;
        this.id = id;
        this.createdAt = createdAt;
        this.author = author;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }
}
