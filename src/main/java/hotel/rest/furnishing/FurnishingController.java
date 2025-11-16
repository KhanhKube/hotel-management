package hotel.rest.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.entity.User;
import hotel.service.furnishing.FurnishingService;
import hotel.util.MessageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer furnishingId,
                       @ModelAttribute("furnishing") Furnishing dto,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttrs){
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
        MessageResponse response = furnishingService.updateFurnishing(furnishingId, dto);
        if(!response.isSuccess()) {
            redirectAttrs.addFlashAttribute("furnishing", dto);
            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel-management/furnishing/edit/" + furnishingId;
        }
        redirectAttrs.addFlashAttribute("message", response.getMessage());
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
        Furnishing furnishing = (Furnishing) model.getAttribute("furnishing");
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
                                     Model model,
                                     RedirectAttributes redirectAttrs) {
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

        MessageResponse response = furnishingService.createFurnishing(dto);

        if(!response.isSuccess()) {
            redirectAttrs.addFlashAttribute("furnishing", dto);
            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel-management/furnishing/create";
        }
        redirectAttrs.addFlashAttribute("message", response.getMessage());
        return "redirect:/hotel-management/furnishing";
    }

    @GetMapping("/update-stock")
    public String showFurnishingUpdatePage(Model model) {
        List<Furnishing> list = furnishingService.findAllAndIsDeletedFalse();
        model.addAttribute("furnishings", list);
        return "management/furnishing/furnishing-update-stock";
    }

    @PostMapping("/update-stock")
    public String updateStock(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
                              @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                              @RequestParam("actionType") String actionType,
                              RedirectAttributes redirectAttrs) {
        // Validate: không chọn dụng cụ nào
        if (selectedIds == null || selectedIds.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Vui lòng chọn ít nhất một dụng cụ.");
            return "redirect:/hotel-management/furnishing/update-stock";
        }

        // Validate: số lượng không nhập
        if (quantities == null || quantities.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Vui lòng nhập số lượng thay đổi.");
            return "redirect:/hotel-management/furnishing/update-stock";
        }
        if (!actionType.equals("ADD") && !actionType.equals("TAKE")) {
            redirectAttrs.addFlashAttribute("error", "Vui lòng chọn phương thức thêm/lấy.");
            return "redirect:/hotel-management/furnishing/update-stock";
        }
        MessageResponse response = furnishingService.updateFurnishingStock(selectedIds, quantities, actionType);
        if (response.isSuccess()) {
            redirectAttrs.addFlashAttribute("message", response.getMessage());
            return "redirect:/hotel-management/furnishing";
        } else {
            redirectAttrs.addFlashAttribute("selectedIds", selectedIds);
            redirectAttrs.addFlashAttribute("quantities", quantities);
            redirectAttrs.addFlashAttribute("actionType", actionType);

            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel-management/furnishing/update-stock";
        }
    }
}
