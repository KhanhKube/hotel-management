package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news")
public class News {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
