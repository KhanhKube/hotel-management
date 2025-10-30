package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports")
public class Report extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}

