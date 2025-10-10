package hotel.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@Table(name = "rooms")
public class Room extends AbstractVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Integer roomId;

	@Column(name = "room_number", nullable = false, unique = true, length = 50)
	private String roomNumber;

	@Column(name = "room_type", length = 100)
	private String roomType;

	@Column(name = "bed_type", length = 100)
	private String bedType;

	@JoinColumn(name = "floor_id")
	private Integer floorId;

	@JoinColumn(name = "size_id")
	private Integer sizeId;

	@Column(name = "room_description", columnDefinition = "TEXT")
	private String roomDescription;

	@Column(name = "price", precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "status", length = 50)
	private String status;

	@Column(name = "sold")
	private Integer sold = 0;

	@Column(name = "view")
	private Integer view = 0;
}

