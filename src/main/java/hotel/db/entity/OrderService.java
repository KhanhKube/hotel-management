package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_services")
public class OrderService extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_service_id")
    private Integer orderServiceId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "service_id")
    private Integer serviceId;
}