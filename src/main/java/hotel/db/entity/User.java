package hotel.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "username", unique = true, length = 50)
	private String username;

	@Column(name = "role", length = 50)
	private String role;

	@Column(name = "email", unique = true, length = 100)
	private String email;

	@Column(name = "password", length = 255)
	private String password;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "first_name", length = 50)
	private String firstName;

	@Column(name = "last_name", length = 50)
	private String lastName;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", columnDefinition = "ENUM('MALE', 'FEMALE', 'OTHER') default 'OTHER'")
	private Gender gender = Gender.OTHER;

	@Column(name = "dob")
	private LocalDate dob;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "avatar_url", length = 255)
	private String avatarUrl;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	@Column(name = "last_logout")
	private LocalDateTime lastLogout;

	@Column(name = "otp", length = 50)
	private String otp;

	@Column(name = "otp_verified")
	private Boolean otpVerified = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') default 'ACTIVE'")
	private Status status = Status.ACTIVE;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

    // --- Enums ---
	public enum Gender {
		MALE, FEMALE, OTHER
	}

	public enum Status {
		ACTIVE, INACTIVE, SUSPENDED
	}
}
