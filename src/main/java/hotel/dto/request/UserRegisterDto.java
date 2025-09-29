package hotel.dto.request;

import hotel.db.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String firstName;
    private String lastName;
    private Boolean gender;
    private LocalDate dob;
    private String address;
}
