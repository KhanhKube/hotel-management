package hotel.service.order;

import hotel.db.dto.checking.OrderMaintenanceResponse;
import hotel.db.dto.order.BookingInfoDto;
import hotel.db.dto.order.OrderDetailDto;
import hotel.db.dto.order.OrderDto;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.util.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

		// Filter out CART status orders (only show actual orders)
		return orders.stream()
				.filter(order -> !"CART".equals(order.getStatus()))
				.map(order -> {
					OrderDto dto = new OrderDto();
					dto.setOrderId(order.getOrderId());
					dto.setUserId(order.getUserId());
					dto.setCheckIn(order.getCheckIn());
					dto.setCheckOut(order.getCheckOut());
					dto.setStatus(order.getStatus());
					dto.setCreatedAt(order.getCreatedAt());
					dto.setPaymentOrderCode(order.getPaymentOrderCode());
					dto.setTotalAmount(order.getTotalAmount());

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
						detailDto.setAmount(detail.getAmount());

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

	@Override
	public Page<OrderMaintenanceResponse> findFutureOrdersByRoomId(int id, LocalDateTime today, Pageable pageable) {
		Page<OrderMaintenanceResponse> orderDetailPageable = orderDetailRepository.findRoomMaintenanceOrders(id, today, pageable);
		if(orderDetailPageable.getTotalElements() > 0) {
			return orderDetailPageable;
		}
		return null;
	}

	@Override
	public Page<OrderMaintenanceResponse> findAllOrderDetailsWithFilter(String search, Boolean isDeleted, Pageable pageable) {
		return orderDetailRepository.findAllOrderDetailsWithFilter(search, isDeleted, pageable);
	}

	@Override
	public MessageResponse cancelOrderDetail(Integer orderId){
		OrderDetail orderDetail = orderDetailRepository.findById(orderId).orElse(null);
		if(orderDetail == null) {
			return new MessageResponse(false, "Không tìm thấy đơn!");
		}
		orderDetail.setStatus("CANCELLED");
		orderDetail.setIsDeleted(true);
		orderDetailRepository.save(orderDetail);
		Order order = orderRepository.findById(orderId).orElse(null);
		if(order == null) {
			return new MessageResponse(false, "Không tìm thấy đơn!");
		}
		BigDecimal amount = orderDetail.getAmount() != null ? orderDetail.getAmount() : BigDecimal.ZERO;
		BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

		BigDecimal newTotal = totalAmount.subtract(amount);
		if(newTotal.compareTo(BigDecimal.ZERO) < 0) {
			newTotal = BigDecimal.ZERO;
		}
		order.setTotalAmount(newTotal);
		orderRepository.save(order);
		return new MessageResponse(true, "Xứ lí thành công, huỷ đơn đặt phòng!");
	}

	@Override
	public MessageResponse updateOrderDetailStatus(Integer orderDetailId, String status) {
		OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElse(null);
		if(orderDetail == null) {
			return new MessageResponse(false, "Không tìm thấy đơn!");
		}
		orderDetail.setStatus(status);
		
		// Nếu trạng thái là CANCEL thì set isDeleted = true
		if("CANCEL".equals(status)) {
			orderDetail.setIsDeleted(true);
		}
		// Nếu trạng thái là RESERVED thì set isDeleted = false
		else if("RESERVED".equals(status)) {
			orderDetail.setIsDeleted(false);
		}
		
		orderDetailRepository.save(orderDetail);
		return new MessageResponse(true, "Cập nhật trạng thái thành công!");
	}

	@Override
	public OrderDetail getOrderDetail(Integer orderId) {
		return orderDetailRepository.findById(orderId).orElse(null);
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
