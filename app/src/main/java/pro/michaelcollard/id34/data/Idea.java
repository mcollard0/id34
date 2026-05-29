package pro.michaelcollard.id34.data;

public class Idea {
    public String id;
    public String content;
    public String createdAt;
    public String updatedAt;
    public int deleted;

    public Idea(String id, String content, String createdAt, String updatedAt, int deleted) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }
}
