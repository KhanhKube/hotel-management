package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String building; // Khối / Toà
    private Integer floor;                              // Tầng
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
