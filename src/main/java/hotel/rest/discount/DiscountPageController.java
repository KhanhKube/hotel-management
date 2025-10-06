package hotel.rest.discount;

import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/discount")
public class DiscountPageController {

    private final DiscountService discountService;

    @GetMapping
    public String view(Model model) {
        // Tạm thời trả về list rỗng để tránh lỗi
        model.addAttribute("listDiscount", discountService.getAll());
        return "management.discount/discountmanage";
    }
    @GetMapping("/create")
    public String createDiscountForm() {
        return "management.discount/discount-create-form";
    }
}


