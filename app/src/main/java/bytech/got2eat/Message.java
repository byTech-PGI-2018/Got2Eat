package bytech.got2eat;

public class Message {
    private boolean self;
    private String content;
    private String user;
    private long timestamp;

    public Message(String content, String user, long timestamp, boolean self) {
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
        this.self = self;
    }

    /*----Getters and setters------*/

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSelf(){
        return self;
    }
}
