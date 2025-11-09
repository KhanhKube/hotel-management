package hotel.db.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
    private String username;
    private String password;
    private String newPassword;
    private String confirmPassword;
}
