//package hotel.db.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "bookings")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class Booking {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Khách
//    @Column(nullable = false) private String customerName;
//    private String customerPhone;
//    private String customerEmail;
//
//    // Thời gian
//    private LocalDateTime checkInDate;
//    private LocalDateTime checkOutDate;
//
//    // Phòng
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_id", nullable = false)
//    private Room room;
//
//    // Đang ở hay đã checkout
//    @Column(nullable = false)
//    private boolean active = true;
//
//    private Double totalAmount;                 // tiền tạm tính/ chốt
//    @Column(columnDefinition = "TEXT")
//    private String servicesJson;                // lưu list dịch vụ dạng chuỗi
//    private String note;                        // log staff/receptionist
//}
