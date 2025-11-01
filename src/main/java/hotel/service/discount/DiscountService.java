package hotel.service.discount;

import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {

    boolean checkVoucherCodeExist(String code);

    Discount findDiscountById(Long id);

    boolean checkDiscountCodeExistExceptItSelft(String code, Long discountId);

    List<DiscountResponseDto> getListDiscount();

    String calculateStatus(Discount d); //Tính giá trị cho status

    Discount getDiscountById(Long discountId);
    
    List<String> getRoomTypesForDiscount();
    
    // Method filter và pagination cho trang quản lý discount
    Page<DiscountResponseDto> getDiscountListForManagement(String search, String discountType, String roomType,
                                                           String status, String sortBy, int page, int pageSize);
}