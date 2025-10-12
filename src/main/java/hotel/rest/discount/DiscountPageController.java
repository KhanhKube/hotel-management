package hotel.rest.discount;

import hotel.db.entity.Discount;
import hotel.db.repository.discount.DiscountRepository;
import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String createDiscount(@ModelAttribute Discount discount) {
        // Set default values
        if (discount.getDiscountId() != null) {
            discountRepository.save(discount);
        } else {
            if (discount.getUsedCount() == null) {
                discount.setUsedCount(0);
            }
            if (discount.getStatus() == null) {
                discount.setStatus("ACTIVE");
            }
            discountRepository.save(discount);
        }
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


