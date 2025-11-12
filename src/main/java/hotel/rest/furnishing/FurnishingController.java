package hotel.rest.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.entity.User;
import hotel.service.furnishing.FurnishingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel-management/furnishing")
@RequiredArgsConstructor
public class FurnishingController {

    private final FurnishingService furnishingService;

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
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
        if(user.getRole().equals(RECEPTIONIST) ) {
            return "redirect:/hotel/dashboard";
        }
        Page<Furnishing> furnishings = furnishingService.findFurnishingFilter(search, sortBy, page, pageSize);
        if(furnishings != null) {
            model.addAttribute("listFurnishings", furnishings.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", furnishings.getTotalPages());
            model.addAttribute("totalElements", furnishings.getTotalElements());
            model.addAttribute("pageSize", pageSize);
            return "management/furnishing/furnishing-list";
        }
        return "redirect:/hotel";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer furnishingId,
                       HttpSession session,
                       Model model){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(RECEPTIONIST) ) {
            return "redirect:/hotel/dashboard";
        }
        Furnishing furnishing = furnishingService.findFurnishingById(furnishingId);
        if(furnishing != null) {
            model.addAttribute("furnishing", furnishing);
            return "management/furnishing/furnishing-detail";
        }
        return "redirect:/hotel-management/furnishing";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer furnishingId,
                       @ModelAttribute("furnishing") Furnishing dto,
                       HttpSession session,
                       Model model){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(RECEPTIONIST) ) {
            return "redirect:/hotel/dashboard";
        }
        Furnishing furnishing = furnishingService.findFurnishingById(furnishingId);
        if(furnishing != null) {
            model.addAttribute("furnishing", furnishing);
            return "management/furnishing/furnishing-detail";
        }
        return "redirect:/hotel-management/furnishing";
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
        if(user.getRole().equals(RECEPTIONIST) ) {
            return "redirect:/hotel/dashboard";
        }
        Furnishing furnishing = (Furnishing) session.getAttribute("furnishing");
        if(furnishing == null) {
            model.addAttribute("furnishing", new Furnishing());
            return "management/furnishing/furnishing-create";
        }
        model.addAttribute("furnishing", furnishing);
        return "management/furnishing/furnishing-create";
    }
    @PostMapping("/create")
    public String createReceptionist(@ModelAttribute("furnishing") Furnishing dto,
                                     HttpSession session,
                                     Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(RECEPTIONIST) ) {
            return "redirect:/hotel/dashboard";
        }
        Furnishing furnishing = (Furnishing) session.getAttribute("furnishing");
        if(furnishing == null) {
            model.addAttribute("furnishing", new Furnishing());
            return "management/furnishing/furnishing-create";
        }
        model.addAttribute("furnishing", furnishing);
        return "management/furnishing/furnishing-create";
    }


}
