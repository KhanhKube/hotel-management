package hotel.rest.receptionist;

import hotel.db.entity.User;
import hotel.service.receptionist.ReceptionistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel-management/receptionist")
@RequiredArgsConstructor
@Slf4j
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        if(user.getRole().equals(STAFF) ||
        user.getRole().equals(RECEPTIONIST) ||
        user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        List<User> receptionist = receptionistService.getListReceptionist();
        if(receptionist != null) {
            model.addAttribute("receptionist", receptionist);
            return "management/receptionist/receptionist-list";
        }
        return "redirect:/hotel/dashboard";
    }
}
