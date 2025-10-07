package hotel.db.dto.user;

import hotel.db.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AccountResponseDto {
    
    private Integer userId;
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private User.Gender gender;
    private LocalDate dob;
    private String address;
    private String avatarUrl;
    private String role;
    private User.Status status;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLogout;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor để convert từ User entity
    public AccountResponseDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.gender = user.getGender();
        this.dob = user.getDob();
        this.address = user.getAddress();
        this.avatarUrl = user.getAvatarUrl();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.lastLogin = user.getLastLogin();
        this.lastLogout = user.getLastLogout();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
