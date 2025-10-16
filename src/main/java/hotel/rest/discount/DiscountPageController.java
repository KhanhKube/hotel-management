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
        model.addAttribute("listDiscount", discountService.getListDiscount());
        return "management/discount/discountmanage";
    }

    @GetMapping("/create")
    public String createDiscountForm(Model model) {
        Discount discount = new Discount();
        discount.setDiscountType("percent");
        model.addAttribute("discount", discount);
        return "management/discount/discount-create-form";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute("discount") Discount discount,
                                 BindingResult result, Model model) {
        if (discount.getDiscountId() == null) {
            // CREATE: check code có tồn tại chưa
            if (discountRepository.existsByCodeAndIsDeletedFalse(discount.getCode())) {
                model.addAttribute("errorMessage", "Mã giảm giá này đã tồn tại!");
                model.addAttribute("discount", discount);
                return "management/discount/discount-create-form";
            }
        } else {
            // UPDATE: check code có trùng với record KHÁC không
            if (discountRepository.existsByCodeAndDiscountIdNotAndIsDeletedFalse(
                    discount.getCode(), discount.getDiscountId())) {
                model.addAttribute("errorMessage", "Mã giảm giá này đã tồn tại!");
                model.addAttribute("discount", discount);
                return "management/discount/discount-create-form";
            }
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
        return "management/discount/discount-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id) {
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