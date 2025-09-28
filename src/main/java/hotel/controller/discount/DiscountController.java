package hotel.controller.discount;
import hotel.db.entity.Discount;
import hotel.dto.request.DiscountRequestDto;
import hotel.service.common.DiscountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {
    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }
    //test
    @PostMapping("/apply")
    public String applyVoucher(@RequestBody DiscountRequestDto request) {
        Discount discount = discountService.applyVoucher(request.getCode());
        return "Áp dụng voucher thành công: " + discount.getCode() +
                " (" + discount.getDiscountType() + " - " + discount.getValue() + ")";
    }
}
