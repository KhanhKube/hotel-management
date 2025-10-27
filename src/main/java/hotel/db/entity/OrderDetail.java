package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_details")
public class OrderDetail extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer orderDetailId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "floor_id")
    private Integer floorId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "order_description", columnDefinition = "TEXT")
    private String orderDescription;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "status")
    private String status = "PENDING"; // Trường hợp mà khách hàng hủy đặt phòng, thì status anh nào co
                                       // de phần này thì để là CANCELLED nhé, hoặc check out xong thì để là COMPLETED nhé.
}