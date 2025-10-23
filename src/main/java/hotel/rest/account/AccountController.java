package hotel.rest.account;

import hotel.db.dto.user.AccountRequestDto;
import hotel.db.dto.user.AccountResponseDto;
import hotel.service.account.AccountService;
import hotel.util.BaseController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/hotel-management/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController extends BaseController {

    private final AccountService accountService;

    /**
     * Hiển thị danh sách tất cả accounts (trừ ADMIN)
     */
    @GetMapping
    public String listAccounts(HttpSession session, Model model) {
        List<AccountResponseDto> accounts = accountService.getAllAccountsExceptAdmin();
        model.addAttribute("accounts", accounts);
        return "management/account/account-list";
    }

    /**
     * Hiển thị danh sách accounts theo role
     */
    @GetMapping("/role/{role}")
    public String listAccountsByRole(@PathVariable String role, HttpSession session, Model model) {
        List<AccountResponseDto> accounts = accountService.getAccountsByRole(role);
        model.addAttribute("accounts", accounts);
        model.addAttribute("role", role);
        return "management/account/account-list";
    }

    /**
     * Hiển thị chi tiết account
     */
    @GetMapping("/{id}")
    public String viewAccount(@PathVariable Integer id, Model model) {
        AccountResponseDto account = accountService.getAccountById(id);
        model.addAttribute("account", account);
        return "management/account/account-details";
    }

    /**
     * Form tạo account mới
     */
    @GetMapping("/new")
    public String createAccountForm(Model model) {
        model.addAttribute("account", new AccountRequestDto());
        return "management/account/account-form";
    }

    /**
     * Lưu account mới
     */
    @PostMapping("/save")
    public String saveAccount(@Valid @ModelAttribute("account") AccountRequestDto accountRequestDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "management/account/account-form";
        }

        try {
            accountService.createAccount(accountRequestDto);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo account thành công!");
            return "redirect:/hotel-management/accounts?success=add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo account: " + e.getMessage());
            return "redirect:/hotel-management/accounts/new";
        }
    }

    /**
     * Form chỉnh sửa account
     */
    @GetMapping("/edit/{id}")
    public String editAccountForm(@PathVariable Integer id, Model model) {
        try {
            AccountResponseDto account = accountService.getAccountById(id);
            
            // Convert ResponseDto to RequestDto for form
            AccountRequestDto accountRequestDto = new AccountRequestDto();
            accountRequestDto.setUserId(account.getUserId());
            accountRequestDto.setUsername(account.getUsername());
            accountRequestDto.setEmail(account.getEmail());
            accountRequestDto.setPhone(account.getPhone());
            accountRequestDto.setFirstName(account.getFirstName());
            accountRequestDto.setLastName(account.getLastName());
            accountRequestDto.setGender(account.getGender());
            accountRequestDto.setDob(account.getDob());
            accountRequestDto.setAddress(account.getAddress());
            accountRequestDto.setRole(account.getRole());
            accountRequestDto.setStatus(account.getStatus());
            accountRequestDto.setAvatarUrl(account.getAvatarUrl());
            
            model.addAttribute("account", accountRequestDto);
            model.addAttribute("accountId", id);
            return "management/account/account-form";
        } catch (Exception e) {
            log.error("Error loading account for edit: {}", id, e);
            return "redirect:/hotel-management/accounts?error=notfound";
        }
    }

    /**
     * Cập nhật account
     */
    @PostMapping("/update/{id}")
    public String updateAccount(@PathVariable Integer id,
                               @Valid @ModelAttribute("account") AccountRequestDto accountRequestDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "management/account/account-form";
        }

        try {
            accountService.updateAccount(id, accountRequestDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật account thành công!");
            return "redirect:/hotel-management/accounts?success=edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật account: " + e.getMessage());
            return "redirect:/hotel-management/accounts/edit/" + id;
        }
    }

    /**
     * Thay đổi trạng thái active của account
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleAccountStatus(@PathVariable Integer id,
                                     RedirectAttributes redirectAttributes) {
        try {
            accountService.toggleAccountStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái account thành công!");
            return "redirect:/hotel-management/accounts?success=toggle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thay đổi trạng thái: " + e.getMessage());
            return "redirect:/hotel-management/accounts";
        }
    }


    /**
     * Xóa tài khoản - SOFT DELETE (ẩn khỏi giao diện)
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteAccount(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("=== BẮT ĐẦU SOFT DELETE TÀI KHOẢN ID: {} ===", id);
            
            // Gọi service soft delete
            accountService.deleteAccount(id);
            
            response.put("success", true);
            response.put("message", "Đã xóa tài khoản khỏi hệ thống!");
            log.info("=== SOFT DELETE THÀNH CÔNG ID: {} - ĐÃ ẨN KHỎI GIAO DIỆN ===", id);
            
        } catch (Exception e) {
            log.error("=== LỖI SOFT DELETE ID: {} - {} ===", id, e.getMessage());
            
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Kiểm tra username đã tồn tại chưa (AJAX)
     */
    @GetMapping("/check-username/{username}")
    @ResponseBody
    public boolean checkUsernameExists(@PathVariable String username) {
        try {
            return accountService.existsByUsername(username);
        } catch (Exception e) {
            log.error("Error checking username: {}", username, e);
            return false;
        }
    }

}
