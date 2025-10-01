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
@Table(name = "furnishings")
public class Furnishing extends AbstractVersion{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "furnishing_id")
    private Integer furnishingId;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "furnishing_description", columnDefinition = "TEXT")
    private String furnishingDescription;

    @Column(name = "quantity")
    private Integer quantity = 1;
}
