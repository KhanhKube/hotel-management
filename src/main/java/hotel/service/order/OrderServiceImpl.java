package hotel.service.order;

import hotel.db.dto.order.BookingInfoDto;
import hotel.db.dto.order.OrderDetailDto;
import hotel.db.dto.order.OrderDto;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<OrderDto> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        
        return orders.stream().map(order -> {
            OrderDto dto = new OrderDto();
            dto.setOrderId(order.getOrderId());
            dto.setUserId(order.getUserId());
            dto.setCheckIn(order.getCheckIn());
            dto.setCheckOut(order.getCheckOut());
            dto.setStatus(order.getStatus());
            dto.setCreatedAt(order.getCreatedAt());
            
            // Get order details
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
            List<OrderDetailDto> detailDtos = orderDetails.stream().map(detail -> {
                OrderDetailDto detailDto = new OrderDetailDto();
                detailDto.setOrderDetailId(detail.getOrderDetailId());
                detailDto.setOrderId(detail.getOrderId());
                detailDto.setRoomId(detail.getRoomId());
                detailDto.setCheckIn(detail.getCheckIn());
                detailDto.setCheckOut(detail.getCheckOut());
                detailDto.setStatus(detail.getStatus());
                detailDto.setOrderDescription(detail.getOrderDescription());
                
                // Get room info
                roomRepository.findById(detail.getRoomId()).ifPresent(room -> {
                    detailDto.setRoomNumber(room.getRoomNumber());
                    detailDto.setRoomType(room.getRoomType());
                    detailDto.setPrice(room.getPrice());
                });
                
                return detailDto;
            }).collect(Collectors.toList());
            
            dto.setOrderDetails(detailDtos);
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BookingInfoDto> getBookingInfoByUserId(Integer userId) {
        List<Object[]> results = orderRepository.findBookingInfoByUserId(userId);
        return mapToBookingInfoDto(results);
    }

    @Override
    public List<BookingInfoDto> getAllBookingInfo() {
        List<Object[]> results = orderRepository.findAllBookingInfo();
        return mapToBookingInfoDto(results);
    }

    private List<BookingInfoDto> mapToBookingInfoDto(List<Object[]> results) {
        List<BookingInfoDto> bookingInfoList = new ArrayList<>();
        
        for (Object[] row : results) {
            BookingInfoDto dto = new BookingInfoDto();
            
            // order_id
            dto.setOrderId(row[0] != null ? ((Number) row[0]).intValue() : null);
            
            // order_detail_id
            dto.setOrderDetailId(row[1] != null ? ((Number) row[1]).intValue() : null);
            
            // check_in (ngày đặt)
            if (row[2] != null) {
                if (row[2] instanceof Timestamp) {
                    dto.setCheckIn(((Timestamp) row[2]).toLocalDateTime());
                } else if (row[2] instanceof LocalDateTime) {
                    dto.setCheckIn((LocalDateTime) row[2]);
                }
            }
            
            // check_out (ngày hết hạn)
            if (row[3] != null) {
                if (row[3] instanceof Timestamp) {
                    dto.setCheckOut(((Timestamp) row[3]).toLocalDateTime());
                } else if (row[3] instanceof LocalDateTime) {
                    dto.setCheckOut((LocalDateTime) row[3]);
                }
            }
            
            // room_number
            dto.setRoomNumber(row[4] != null ? row[4].toString() : null);
            
            // room_type
            dto.setRoomType(row[5] != null ? row[5].toString() : null);
            
            // status
            dto.setStatus(row[6] != null ? row[6].toString() : null);
            
            // created_at
            if (row[7] != null) {
                if (row[7] instanceof Timestamp) {
                    dto.setCreatedAt(((Timestamp) row[7]).toLocalDateTime());
                } else if (row[7] instanceof LocalDateTime) {
                    dto.setCreatedAt((LocalDateTime) row[7]);
                }
            }
            
            bookingInfoList.add(dto);
        }
        
        return bookingInfoList;
    }
}
