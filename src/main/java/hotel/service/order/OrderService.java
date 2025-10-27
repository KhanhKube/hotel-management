package hotel.service.order;

import hotel.db.dto.order.BookingInfoDto;
import hotel.db.dto.order.OrderDto;

import java.util.List;

public interface OrderService {
    List<OrderDto> getOrdersByUserId(Integer userId);
    List<BookingInfoDto> getBookingInfoByUserId(Integer userId);
    List<BookingInfoDto> getAllBookingInfo();
}
