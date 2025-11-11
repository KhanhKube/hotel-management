package hotel.rest.discount;

import hotel.db.entity.Discount;
import hotel.db.repository.discount.DiscountRepository;
import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
            Model model) {
        
        org.springframework.data.domain.Page<hotel.db.dto.discount.DiscountResponseDto> discountPage = 
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
    public String createDiscountForm(Model model) {
        Discount discount = new Discount();
        model.addAttribute("discount", discount);
        model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
        return "management/discount/discount-create-form";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute("discount") Discount discount,
                                 BindingResult result, Model model) {
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
    public String editDiscountForm(@PathVariable Long id, Model model) {
        Discount discount =  discountService.getDiscountById(id);
        if  (discount == null) {
            return "redirect:/hotel-management/discount";
        }
        model.addAttribute("discount", discount);
        model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
        return "management/discount/discount-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id, Model model) {
        // Lấy discount để check status
        Discount discount = discountService.getDiscountById(id);
        
        if (discount == null) {
            model.addAttribute("errorMessage", "Không tìm thấy mã giảm giá!");
            model.addAttribute("listDiscount", discountService.getListDiscount());
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discountmanage";
        }
        
        // Tính status
        String status = discountService.calculateStatus(discount);
        
        // Không cho xóa nếu đang ACTIVE
        if ("ACTIVE".equals(status)) {
            model.addAttribute("errorMessage", "Không thể xóa mã giảm giá đang hoạt động! Vui lòng đợi đến khi hết hạn hoặc hết số lượng.");
            model.addAttribute("listDiscount", discountService.getListDiscount());
            model.addAttribute("roomTypes", discountService.getRoomTypesForDiscount());
            return "management/discount/discountmanage";
        }
        
        // Cho phép xóa nếu PENDING, EXPIRED, hoặc EXHAUSTED
        discountRepository.softDeleteById(id);
        return "redirect:/hotel-management/discount";
    }

    @GetMapping("/detail/{id}")
    public String detailDiscount(@PathVariable Long id, Model model) {
        Discount discount = discountService.getDiscountById(id);
        model.addAttribute("discount", discount);
        return "management/discount/discount-detail";
    }
}