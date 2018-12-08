package bytech.got2eat;

import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

public class Message implements IMessage {
    private String text;
    private String id;
    private Date createdAt;
    private Author author;
    private String firestoreId = null;

    public Message(String text, String id, Date createdAt, Author author) {
        this.text = text;
        this.id = id;
        this.createdAt = createdAt;
        this.author = author;
    }

    public Message(String text, String id, Date createdAt, Author author, String firestoreId){
        this.text = text;
        this.id = id;
        this.createdAt = createdAt;
        this.author = author;
        this.firestoreId = firestoreId;
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

    public String getFirestoreId() {
        return firestoreId;
    }
}


