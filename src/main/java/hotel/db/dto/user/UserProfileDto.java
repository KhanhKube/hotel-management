package hotel.db.dto.user;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String username;
    private String email;
    private String avatarUrl;
    private String phone;
    private String firstName;
    private String lastName;
    private Boolean gender;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String address;
    private LocalDateTime createdAt;
}
