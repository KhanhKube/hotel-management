package hotel.service.discount;

import hotel.db.dto.discount.DiscountResponseDto;
import hotel.db.entity.Discount;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DiscountService {

    boolean checkVoucherCodeExist(String code);


    boolean checkDiscountCodeExistExceptItSelft(String code, Long discountId);

    List<DiscountResponseDto> getListDiscount();

    String calculateStatus(Discount d); //Tính giá trị cho status

    Discount getDiscountById(Long discountId);
    
    List<String> getRoomTypesForDiscount();
    
    // Method filter và pagination cho trang quản lý discount
    Page<DiscountResponseDto> getDiscountListForManagement(String search, String discountType, String roomType,
                                                           String status, String sortBy, int page, int pageSize);
}