package hotel.rest.staff;

import hotel.db.dto.staff.StaffListDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.service.receptionist.ReceptionistService;
import hotel.service.staff.StaffService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel-management/staff")
@RequiredArgsConstructor
public class StaffController {
    private final UserRepository userRepo;
    private final ReceptionistService receptionistService;
    private final StaffService staffService;

    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("status", User.Status.values());
        model.addAttribute("gender", User.Gender.values());
    }

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        Page<StaffListDto> staffPage = staffService.getStaffListForManagement(
                search, role, gender, status, sortBy, page, pageSize);

        model.addAttribute("listUser", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("totalElements", staffPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        return "management/staff/staff-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        model.addAttribute("user", new User());
        return "management/staff/staff-create";
    }

    @PostMapping("/create")
    public String createStaff(@ModelAttribute User user, Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user1 = (User) session.getAttribute("user");
        if (user1 == null) return "redirect:/hotel";
        if (user1.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user1.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        String errorMessage = staffService.createStaffFromForm(user);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("user", user);
            return "management/staff/staff-create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Tạo nhân viên thành công!");
        return "redirect:/hotel-management/staff";
    }

    

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable Integer id, Model model, HttpSession session) {
        User user1 = (User) session.getAttribute("user");
        if (user1 == null) return "redirect:/hotel";
        if (user1.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user1.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        User user = staffService.getStaff(id);
        if (user == null) {
            return "redirect:/hotel-management/staff";
        }
        model.addAttribute("user", user);
        return "management/staff/staff-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, HttpSession session, Model model) {
        User user1 = (User) session.getAttribute("user");
        if (user1 == null) return "redirect:/hotel";
        if (user1.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user1.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        User user = staffService.getStaff(id);
        if (user == null) {
            return "redirect:/hotel-management/staff";
        }
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        return "management/staff/staff-create";
    }

    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable Integer id, @ModelAttribute User user, Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user1 = (User) session.getAttribute("user");
        if (user1 == null) return "redirect:/hotel";
        if (user1.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user1.getRole().equals(MANAGER)) return "redirect:/hotel/dashboard";

        user.setUserId(id);
        String errorMessage = staffService.updateStaffFromForm(user);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("user", user);
            model.addAttribute("isEdit", true);
            return "management/staff/staff-create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
        return "redirect:/hotel-management/staff";
    }

}
