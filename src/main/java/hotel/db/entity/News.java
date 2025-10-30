package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "[view]")
    private Integer view;

    @Column(name = "link", length = 255)
    private String link;

    @Column(name = "status", length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED
}