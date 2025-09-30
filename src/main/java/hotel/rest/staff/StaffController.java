package hotel.rest.staff;

import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final UserRepository userRepo;

    @GetMapping
    public List<User> list(@RequestParam(defaultValue = "HOUSEKEEPING") String role) {
        return userRepo.findByRole(role);
    }
}
