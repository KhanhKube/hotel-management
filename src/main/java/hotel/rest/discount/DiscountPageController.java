package hotel.rest.discount;

import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/discount")
public class DiscountPageController {

    private final DiscountService discountService;

    @GetMapping
    public String view() {

        return "management.discount/discountmanage";
    }
}


