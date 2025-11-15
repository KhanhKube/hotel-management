package hotel.service.order;

import hotel.db.dto.checking.OrderMaintenanceResponse;
import hotel.db.dto.order.BookingInfoDto;
import hotel.db.dto.order.OrderDto;
import hotel.db.entity.OrderDetail;
import hotel.util.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    List<OrderDto> getOrdersByUserId(Integer userId);
    List<BookingInfoDto> getBookingInfoByUserId(Integer userId);
    List<BookingInfoDto> getAllBookingInfo();
    Page<OrderMaintenanceResponse> findFutureOrdersByRoomId(int id, LocalDateTime today, Pageable pageable);
    MessageResponse cancelOrderDetail(Integer orderId);
    OrderDetail getOrderDetail(Integer orderId);
}
