package hotel.db.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpDto {
    private String username;
    private String email;
    private String otp;
}
