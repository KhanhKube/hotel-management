package hotel.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_views")
public class RoomView extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_view_id")
    private Integer roomViewId;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "view_id")
    private Integer viewId;
}