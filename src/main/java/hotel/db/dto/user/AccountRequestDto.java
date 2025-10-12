package hotel.db.dto.user;

import hotel.db.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountRequestDto {
    
    private Integer userId;
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải có từ 3-50 ký tự")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số")
    private String phone;
    
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự")
    private String firstName;
    
    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ không được quá 50 ký tự")
    private String lastName;
    
    @NotNull(message = "Giới tính không được để trống")
    private User.Gender gender;
    
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dob;
    
    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;
    
    @NotBlank(message = "Vai trò không được để trống")
    private String role;
    
    @NotNull(message = "Trạng thái không được để trống")
    private User.Status status;
    
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    private String avatarUrl;
}
