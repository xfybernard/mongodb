package pojo;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="comments")
public class Comments {
	private String author;
	
	private Date commentTime;
	
	private String content;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCommentTime() {
		return commentTime;
	}

	public void setCommentTime(Date commentTime) {
		this.commentTime = commentTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
