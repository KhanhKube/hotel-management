package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", columnDefinition = "ENUM('PERCENT', 'AMOUNT') NOT NULL")
    private DiscountType discountType;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "room_type", nullable = false, length = 100)
    private String roomType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'")
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // --- Enum ---
    public enum DiscountType {
        PERCENT, AMOUNT
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}
