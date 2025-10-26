package hotel.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_images")
public class RoomImage extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_image_id")
    @JsonProperty("roomImageId")
    private Integer roomImageId;

    @Column(name = "room_id")
    @JsonProperty("roomId")
    private Integer roomId;

    @Column(name = "room_image_url", length = 255)
    @JsonProperty("roomImageUrl")
    private String roomImageUrl;
}
