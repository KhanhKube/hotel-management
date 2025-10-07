package hotel.rest.discount;

import hotel.db.entity.Discount;
import hotel.db.repository.discount.DiscountRepository;
import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        if (discount.getUsedCount() == null) {
            discount.setUsedCount(0);
        }
        if (discount.getStatus() == null) {
            discount.setStatus("ACTIVE");
        }
        discountRepository.save(discount);
        return "redirect:/hotel-management/discount";
    }
}


