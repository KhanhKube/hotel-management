package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news")
public class News extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Integer newsId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "news_group_id")
    private Integer newsGroupId;

    @Column(name = "heading", length = 255)
    private String heading;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "view")
    private Integer view;

    @Column(name = "link", length = 255)
    private String link;


    @Column(name = "status", length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED

    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNewsGroupId() {
        return newsGroupId;
    }

    public void setNewsGroupId(Integer newsGroupId) {
        this.newsGroupId = newsGroupId;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getView() {
        return view;
    }

    public void setView(Integer view) {
        this.view = view;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
