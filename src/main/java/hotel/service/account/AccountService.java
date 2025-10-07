package hotel.service.account;

import hotel.db.dto.user.AccountRequestDto;
import hotel.db.dto.user.AccountResponseDto;

import java.util.List;

public interface AccountService {
    
    /**
     * Lấy danh sách tất cả accounts
     */
    List<AccountResponseDto> getAllAccounts();
    
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
     * Xóa account (hard delete)
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
}
