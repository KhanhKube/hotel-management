package hotel.rest.receptionist;

import hotel.db.entity.User;
import hotel.service.receptionist.ReceptionistService;
import hotel.util.MessageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel-management/receptionist")
@RequiredArgsConstructor
@Slf4j
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean gender,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(STAFF) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        Page<User> receptionist = receptionistService.getReceptionistListWithFiltersAndPagination(search, gender, status, sortBy, page, pageSize);
        if(receptionist != null) {
            model.addAttribute("listUser", receptionist.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", receptionist.getTotalPages());
            model.addAttribute("totalElements", receptionist.getTotalElements());
            model.addAttribute("pageSize", pageSize);
            return "management/receptionist/receptionist-list";
        }
        return "redirect:/hotel";
    }

    @GetMapping("/edit/{id}")
    public String receptionistDetail(@PathVariable("id") Integer id,
                                     HttpSession session,
                                     Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(STAFF) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        User receptionist = receptionistService.getReceptionist(id);
        if(receptionist != null) {
            model.addAttribute("receptionist", receptionist);
            return "management/receptionist/receptionist-detail";
        }
        return "redirect:/hotel/dashboard";
    }

    @PostMapping("/edit/{id}")
    public String updateReceptionist(@PathVariable("id") Integer id,
                                     @ModelAttribute("receptionist") User dto,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttrs) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(STAFF) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        MessageResponse response = receptionistService.updateReceptionist(id, dto);
        if (response.isSuccess()) {
            model.addAttribute("message", response.getMessage());
            redirectAttrs.addFlashAttribute("message", response.getMessage());
            return "redirect:/hotel-management/receptionist";
        } else {
            model.addAttribute("error", response.getMessage());
            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel-management/receptionist/edit/"+id;
        }
    }

    @GetMapping("/create")
    public String createReceptionist(HttpSession session,
                                     Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(STAFF) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }

//        if(receptionist != null) {
//            model.addAttribute("receptionist", receptionist);
//            return "management/receptionist/receptionist-detail";
//        }
        return "redirect:/hotel/dashboard";
    }

}
