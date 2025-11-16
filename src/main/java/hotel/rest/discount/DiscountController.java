package hotel.rest.discount;

import hotel.db.dto.discount.DiscountResponseDto;
import hotel.db.entity.Discount;
import hotel.db.entity.User;
import hotel.db.repository.discount.DiscountRepository;
import hotel.service.discount.DiscountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hotel.db.enums.Constants.*;
import static hotel.db.enums.Constants.ADMIN;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/discount")
public class DiscountController {

    private final DiscountService discountService;
    private final DiscountRepository discountRepository;

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        Page<DiscountResponseDto> discountPage = 
                discountService.getDiscountListForManagement(search, roomType, status, sortBy, page, pageSize);
        
        model.addAttribute("listDiscount", discountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", discountPage.getTotalPages());
        model.addAttribute("totalElements", discountPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
        
        return "management/discount/discountmanage";
    }

    @GetMapping("/create")
    public String createDiscountForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        Discount discount = new Discount();
        model.addAttribute("discount", discount);
        model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
        return "management/discount/discount-create-form";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute("discount") Discount discount,
                                 BindingResult result, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        // Validate mã giảm giá
        if (discount.getCode() == null || discount.getCode().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập mã giảm giá!");
            model.addAttribute("discount", discount);
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discount-create-form";
        }
        
        if (discount.getCode().length() > 20) {
            model.addAttribute("errorMessage", "Mã giảm giá không được vượt quá 20 ký tự!");
            model.addAttribute("discount", discount);
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discount-create-form";
        }
        
        // Validate số tiền giảm
        if (discount.getValue() == null) {
            model.addAttribute("errorMessage", "Vui lòng nhập số tiền giảm!");
            model.addAttribute("discount", discount);
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discount-create-form";
        }
        
        if (discount.getValue() < 10000) {
            model.addAttribute("errorMessage", "Số tiền giảm tối thiểu là 10.000 VNĐ!");
            model.addAttribute("discount", discount);
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discount-create-form";
        }
        
        if (discount.getValue() > 10000000) {
            model.addAttribute("errorMessage", "Số tiền giảm tối đa là 10.000.000 VNĐ!");
            model.addAttribute("discount", discount);
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discount-create-form";
        }
        
        if (discount.getDiscountId() == null) {
            // CREATE: check code có tồn tại chưa
            if (discountService.checkVoucherCodeExist(discount.getCode())) {
                model.addAttribute("errorMessage", "Mã giảm giá này đã tồn tại!");
                model.addAttribute("discount", discount);
                model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
                return "management/discount/discount-create-form";
            }
        } else {
            // UPDATE: check code có trùng với record KHÁC không
            if (discountService.checkDiscountCodeExistExceptItSelft(
                    discount.getCode(), discount.getDiscountId())) {
                model.addAttribute("errorMessage", "Mã giảm giá này đã tồn tại!");
                model.addAttribute("discount", discount);
                model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
                return "management/discount/discount-create-form";
            }
        }

        discountRepository.save(discount);
        return "redirect:/hotel-management/discount";
    }

    @GetMapping("/edit/{id}")
    public String editDiscountForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        Discount discount =  discountService.getDiscountById(id);
        if  (discount == null) {
            return "redirect:/hotel-management/discount";
        }
        model.addAttribute("discount", discount);
        model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
        return "management/discount/discount-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        
        // Admin có toàn quyền xóa mã giảm giá bất kể trạng thái
        discountRepository.softDeleteById(id);
        return "redirect:/hotel-management/discount";
    }

    @GetMapping("/detail/{id}")
    public String detailDiscount(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN) ||
                user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel/dashboard";
        }
        Discount discount = discountService.getDiscountById(id);
        model.addAttribute("discount", discount);
        return "management/discount/discount-detail";
    }
}