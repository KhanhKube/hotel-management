package hotel.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends AbstractVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Integer orderId;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "floor_id")
	private Integer floorId;

	@Column(name = "check_in")
	private LocalDateTime checkIn;

	@Column(name = "check_out")
	private LocalDateTime checkOut;

	@Column(name = "status")
	private String status;

	@Column(name = "payment_order_code")
	private Long paymentOrderCode;
}

