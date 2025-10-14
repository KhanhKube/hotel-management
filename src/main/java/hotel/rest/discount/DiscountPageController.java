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
public class DiscountPageController {

    private final DiscountService discountService;
    private final DiscountRepository discountRepository;

    @GetMapping
    public String view(Model model) {
        // Tạm thời trả về list rỗng để tránh lỗi
        model.addAttribute("listDiscount", discountService.getAll());
        return "management.discount/discountmanage";
    }

    @GetMapping("/create")
    public String createDiscountForm(Model model) {
        model.addAttribute("discount", new Discount());
        return "management.discount/discount-create-form";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute("discount") Discount discount,
                                 BindingResult result, Model model) {
        if (discountRepository.existsByCodeAndIsDeletedFalse(discount.getCode())) {
            model.addAttribute("errorMessage", "Mã giảm giá này đã tồn tại, Vui lòng nhập mã khác!");
            model.addAttribute("discount", discount);
            return "management.discount/discount-create-form";
        }

        discountRepository.save(discount);
        return "redirect:/hotel-management/discount";
    }
    @GetMapping("/edit/{id}")
    public String editDiscountForm(@PathVariable Long id, Model model) {
        Discount discount =  discountRepository.findById(id).orElse(null);;
        if  (discount == null) {
            return "redirect:/hotel-management/discount";
        }
        model.addAttribute("discount", discount);
        return "management.discount/discount-create-form";
    }
}


