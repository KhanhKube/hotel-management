package hotel.db.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDto {
    private int totalOrders;
    private int selectedOrders;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<Integer> selectedOrderIds;
    private String discountMessage;
    private Boolean discountValid;
}
