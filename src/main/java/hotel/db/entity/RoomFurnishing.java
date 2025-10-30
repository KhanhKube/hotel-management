package hotel.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_furnishings")
public class RoomFurnishing extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_furnishing_id")
    private Integer roomFurnishingId;

    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @Column(name = "furnishing_id", nullable = false)
    private Integer furnishingId;

    @Column(name = "quantity")
    private Integer quantity = 1;
}