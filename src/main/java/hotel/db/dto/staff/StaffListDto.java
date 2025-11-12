package hotel.db.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffListDto {
    private Integer userId;
    private String fullName;        // firstName + " " + lastName
    private String codeStaff;
    private String phone;
    private String email;
    private String gender;
    private String role;
    private BigDecimal salary;
    private String status;
    
    // Constructor để dùng trong JPQL query
    public StaffListDto(Integer userId, String firstName, String lastName, 
                        String codeStaff, String phone, String email, 
                        String gender, String role, BigDecimal salary, String status) {
        this.userId = userId;
        this.fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        this.fullName = this.fullName.trim(); // Xóa khoảng trắng thừa
        this.codeStaff = codeStaff;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.role = role;
        this.salary = salary;
        this.status = status;
    }
}
