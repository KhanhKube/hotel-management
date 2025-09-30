package hotel.db.entity;

import hotel.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String roomNumber; // "101", "A203"...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    private String type;      // Standard/Deluxe/Suite...
    private Integer floor;
    private Double basePrice;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private RoomLocation location;
}
