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
@Table(name = "services")
public class Service extends AbstractVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "service_name", length = 150)
    private String serviceName;

    @Column(name = "service_description", columnDefinition = "TEXT")
    private String serviceDescription;

    @Column(name = "price")
    private Double price;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "opening_hours", length = 100)
    private String openingHours;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

}
