package hotel.db.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Tên khách sạn
    private String address;     // Địa chỉ
    private String phone;       // Số điện thoại
    private String email;       // Email
    private String description; // Mô tả
    private Integer stars;      // Số sao (3,4,5 sao)

    private String services;    // Danh sách dịch vụ (có thể là JSON hoặc 1 bảng riêng)
}
