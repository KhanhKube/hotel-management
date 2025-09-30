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
    private final UserRepository userRepo;

    @GetMapping
    public List<User> getAll(@RequestParam(required = false) String role) {
        return role == null ? userRepo.findAll() : userRepo.findByRole( (role));
    }

    @PatchMapping("/{id}/status")
    public User changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        User u = userRepo.findById(Math.toIntExact(id)).orElseThrow();
        u.setActive(active);
        return userRepo.save(u);
    }
}
