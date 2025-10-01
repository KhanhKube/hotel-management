package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "floors")
public class Floor extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_id")
    private Integer floorId;

    @Column(name = "floor_number")
    private Integer floorNumber;
}
