package org.example.demo.model;

public class PostCreateDao {
    private String writer;
    private String title;
    private String contents;

    public PostCreateDao(String writer, String title, String contents) {
        this.writer = writer;
        this.title = title;
        this.contents = contents;
    }

    public String getWriter() {
        return writer;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }
}
