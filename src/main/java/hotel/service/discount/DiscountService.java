package hotel.service.discount;

import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {

    List<DiscountResponseDto> getListDiscount();

    String calculateStatus(Discount d); //Tính giá trị cho status

    Discount getDiscountById(Long discountId);
    
    List<String> getRoomTypesForDiscount();
}