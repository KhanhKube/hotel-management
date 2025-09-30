package hotel.rest.account;

import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
//
//    private final UserRepository userRepo;
//
//    // Lấy tất cả user hoặc lọc theo role
//    @GetMapping
//    public List<User> getAll(@RequestParam(required = false) String role) {
//        return role == null ? userRepo.findAll() : userRepo.findByRole(role);
//    }
//
//    // Đổi trạng thái active/inactive
//    @PatchMapping("/{id}/status")
//    public User changeStatus(@PathVariable Integer id, @RequestParam boolean active) {
//        User u = userRepo.findById(id).orElseThrow();
//        u.setActive(active);   // ✅ thiếu setActive
//        return userRepo.save(u);
//    }
}


