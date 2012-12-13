package cz.cvut.fel.via.client;

import java.util.Date;

public class Message
{
    private Integer id;
    private String  author;
    private String  content;
    private Date    timestamp;

    public Message()
    {
    }

    public Message(String author, String content)
    {
        this(null, author, content, null);
    }

    public Message(String author, String content, Date timestamp)
    {
        this(null, author, content, null);
    }
    
    public Message(Integer id, String author, String content)
    {
        this(id, author, content, null);
    }

    public Message(Integer id, String author, String content, Date timestamp)
    {
        this.id        = id;
        this.author    = author;
        this.content   = content;
        this.timestamp = timestamp;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String message)
    {
        this.content = message;
    }

    public Date getTimestamp()
    {        
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }
}
