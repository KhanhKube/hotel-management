package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_groups")
public class NewsGroup extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_group_id")
    private Integer newsGroupId;

    @Column(name = "name", length = 150)
    private String name;

    public Integer getNewsGroupId() {
        return newsGroupId;
    }

    public void setNewsGroupId(Integer newsGroupId) {
        this.newsGroupId = newsGroupId;
    }
}
