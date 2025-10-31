package hotel.service.account;

import hotel.db.dto.user.AccountRequestDto;
import hotel.db.dto.user.AccountResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AccountService {
    
    /**
     * Lấy danh sách tất cả accounts
     */
    List<AccountResponseDto> getAllAccounts();
    
    /**
     * Lấy danh sách tất cả accounts (trừ ADMIN)
     */
    List<AccountResponseDto> getAllAccountsExceptAdmin();
    
    /**
     * Lấy danh sách accounts với pagination và search
     */
    Page<AccountResponseDto> getAccountsWithPagination(String searchTerm, String status, int page, int size);
    
    /**
     * Lấy danh sách accounts theo role
     */
    List<AccountResponseDto> getAccountsByRole(String role);
    
    /**
     * Lấy chi tiết account theo ID
     */
    AccountResponseDto getAccountById(Integer userId);
    
    /**
     * Tạo account mới
     */
    AccountResponseDto createAccount(AccountRequestDto accountRequestDto);
    
    /**
     * Cập nhật account
     */
    AccountResponseDto updateAccount(Integer userId, AccountRequestDto accountRequestDto);
    
    /**
     * Thay đổi trạng thái active của account
     */
    AccountResponseDto toggleAccountStatus(Integer userId);
    
    /**
     * Xóa account (soft delete - set isDeleted = true và status = INACTIVE)
     */
    void deleteAccount(Integer userId);
    
    /**
     * Kiểm tra username đã tồn tại chưa
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Lấy danh sách customers với pagination và search
     */
    Page<AccountResponseDto> getCustomersWithPagination(String searchTerm, String status, int page, int size);
    
    /**
     * Lấy danh sách staffs (STAFF, RECEPTIONIST, MANAGER) với pagination và search
     */
    Page<AccountResponseDto> getStaffsWithPagination(String searchTerm, String status, int page, int size);
    
    /**
     * Xóa vĩnh viễn tài khoản (HARD DELETE) - Chỉ dùng cho xóa tài khoản test
     */
    void deleteAllTestAccounts(Integer userId);
}
