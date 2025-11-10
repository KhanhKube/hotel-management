package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts")
public class Discount extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
    private String description;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "room_type", nullable = false, length = 100)
    private String roomType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "used_count", columnDefinition = "INT DEFAULT 0")
    private Integer usedCount = 0;
}