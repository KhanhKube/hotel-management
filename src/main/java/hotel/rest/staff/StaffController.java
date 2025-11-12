package hotel.rest.staff;

import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.service.receptionist.ReceptionistService;
import hotel.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hotel-management/staff")
@RequiredArgsConstructor
public class StaffController {
    private final UserRepository userRepo;
    private final ReceptionistService receptionistService;
    private final StaffService staffService;

    //    @GetMapping
//    public List<User> list(@RequestParam(defaultValue = "HOUSEKEEPING") String role) {
//        return userRepo.findByRole(role);
//    }
    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("status", User.Status.values());
        model.addAttribute("gender", User.Gender.values());
    }

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        System.out.println(gender);
        System.out.println(status);
        System.out.println(sortBy);
        Page<User> users = staffService.getUserListWithFiltersAndPagination(search, gender, status, sortBy, page, pageSize);


        model.addAttribute("listUser", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalElements", users.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        return "management/staff/staff-list";
    }


}
