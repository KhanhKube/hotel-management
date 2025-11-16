package hotel.service.checking;

import hotel.db.dto.checking.*;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.db.enums.OrderDetailStatus;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckingServiceImpl implements CheckingService {

	private final OrderDetailRepository orderDetailRepository;
	private final RoomRepository roomRepository;
	private final UserRepository userRepository;

	@Override
	public Page<OrderDetailResponse> getReservedOrders(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		Page<OrderDetail> reservedOrders = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.RESERVED, pageable);

		// CHỈ LỌC để hiển thị các đơn đang diễn ra (start_date <= now < end_date)
		// KHÔNG tự động đánh dấu expired để không ảnh hưởng đến trang quản lý đơn
		List<OrderDetail> activeOrders = new ArrayList<>();

		for (OrderDetail order : reservedOrders.getContent()) {
			if (isOrderActiveForCheckIn(order, now)) {
				activeOrders.add(order);
			}
			// Không gọi markOrderAsExpired() nữa - để trang quản lý đơn tự xử lý
		}

		List<OrderDetailResponse> responses = activeOrders.stream()
				.map(this::convertToResponse)
				.toList();

		return new PageImpl<>(responses, pageable, activeOrders.size());
	}

	/**
	 * Kiểm tra đơn có đang trong thời gian check-in không
	 * Chỉ dùng cho trang check-in/check-out
	 * Điều kiện: start_date <= now < end_date (đơn đang diễn ra)
	 */
	private boolean isOrderActiveForCheckIn(OrderDetail order, LocalDateTime now) {
		if (order.getStartDate() == null || order.getEndDate() == null) {
			return false;
		}
		// Chỉ hiển thị đơn đang diễn ra: đã đến giờ check-in và chưa hết hạn
		return !order.getStartDate().isAfter(now) && now.isBefore(order.getEndDate());
	}

	@Override
	@Transactional
	public OrderDetailResponse startCheckIn(CheckInRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.RESERVED.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in RESERVED status");
		}

		orderDetail.setStatus(OrderDetailStatus.CHECKING_IN);
		orderDetailRepository.save(orderDetail);
		return convertToResponse(orderDetail);
	}

	@Override
	@Transactional
	public OrderDetailResponse confirmCheckIn(CheckInConfirmRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.CHECKING_IN.equals(orderDetail.getStatus())
				&& !OrderDetailStatus.CUSTOMER_CONFIRM.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Invalid status for check-in confirmation");
		}

		if ("CUSTOMER".equals(request.getConfirmedBy())) {
			orderDetail.setStatus(OrderDetailStatus.CUSTOMER_CONFIRM);
			orderDetailRepository.save(orderDetail);
		} else if ("STAFF".equals(request.getConfirmedBy())) {
			// Staff xác nhận: phải có customer confirm trước
			if (OrderDetailStatus.CUSTOMER_CONFIRM.equals(orderDetail.getStatus())) {
				orderDetail.setStatus(OrderDetailStatus.OCCUPIED);
				orderDetail.setCheckIn(LocalDateTime.now()); // Set thời gian check-in thực tế
				orderDetailRepository.save(orderDetail);

				Room room = roomRepository.findById(orderDetail.getRoomId())
						.orElseThrow(() -> new RuntimeException("Room not found"));
				room.setStatus(RoomStatus.OCCUPIED);
				roomRepository.save(room);
			} else {
				throw new RuntimeException("Customer must confirm first");
			}
		}

		return convertToResponse(orderDetail);
	}

	@Override
	public Page<OrderDetailResponse> getOccupiedOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.OCCUPIED, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	@Transactional
	public OrderDetailResponse startCheckOut(CheckOutRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.OCCUPIED.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in OCCUPIED status");
		}

		orderDetail.setStatus(OrderDetailStatus.NEED_CHECKOUT);
		orderDetailRepository.save(orderDetail);
		return convertToResponse(orderDetail);
	}

	@Override
	public Page<OrderDetailResponse> getNeedCheckOutOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.NEED_CHECKOUT, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	@Transactional
	public OrderDetailResponse staffCheckOut(StaffCheckOutRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.NEED_CHECKOUT.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in NEED_CHECKOUT status");
		}

		// Bước 1: Chuyển sang CHECKING_OUT (tạm thời để LT biết đang kiểm tra)
		orderDetail.setStatus(OrderDetailStatus.CHECKING_OUT);
		orderDetailRepository.save(orderDetail);

		// Bước 2: Nhân viên kiểm tra và gửi form
		orderDetail.setStatus(OrderDetailStatus.CHECKED_OUT);
		orderDetail.setCheckOut(LocalDateTime.now());

		// Lưu ghi chú nếu có vấn đề
		if (request.getHasIssue() != null && request.getHasIssue()) {
			String currentDesc = orderDetail.getOrderDescription() != null ? orderDetail.getOrderDescription() : "";
			orderDetail.setOrderDescription(currentDesc + "\n[STAFF REPORT] " + request.getReportNote());
		}

		orderDetailRepository.save(orderDetail);
		return convertToResponse(orderDetail);
	}

	@Override
	public Page<OrderDetailResponse> getCheckingInOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.CHECKING_IN, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	public Page<OrderDetailResponse> getCustomerConfirmedOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.CUSTOMER_CONFIRM, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	public Page<OrderDetailResponse> getCheckingOutOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.CHECKING_OUT, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	public Page<OrderDetailResponse> getCheckedOutOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.CHECKED_OUT, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	public Page<OrderDetailResponse> getNeedCleanOrdersForStaff(Pageable pageable) {
		return getNeedCleanOrders(pageable);
	}

	@Override
	public Page<OrderDetailResponse> getCleaningOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.CLEANING, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	@Transactional
	public OrderDetailResponse receptionistConfirmCheckOut(AfterCheckOutConfirmRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.CHECKED_OUT.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in CHECKED_OUT status");
		}

		orderDetail.setStatus(OrderDetailStatus.NEED_CLEAN);
		orderDetailRepository.save(orderDetail);

		Room room = roomRepository.findById(orderDetail.getRoomId())
				.orElseThrow(() -> new RuntimeException("Room not found"));
		room.setStatus(RoomStatus.NEED_CLEAN);
		roomRepository.save(room);
		return convertToResponse(orderDetail);
	}

	@Override
	public Page<OrderDetailResponse> getNeedCleanOrders(Pageable pageable) {
		Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
				OrderDetailStatus.NEED_CLEAN, pageable);
		return orderDetails.map(this::convertToResponse);
	}

	@Override
	@Transactional
	public OrderDetailResponse startCleaning(CleaningRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.NEED_CLEAN.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in NEED_CLEAN status");
		}

		orderDetail.setStatus(OrderDetailStatus.CLEANING);
		orderDetailRepository.save(orderDetail);

		Room room = roomRepository.findById(orderDetail.getRoomId())
				.orElseThrow(() -> new RuntimeException("Room not found"));
		room.setStatus(RoomStatus.CLEANING);
		roomRepository.save(room);
		return convertToResponse(orderDetail);
	}

	@Override
	@Transactional
	public OrderDetailResponse completeCleaning(CleaningRequest request) {
		OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
				.orElseThrow(() -> new RuntimeException("Order detail not found"));

		if (!OrderDetailStatus.CLEANING.equals(orderDetail.getStatus())) {
			throw new RuntimeException("Order detail must be in CLEANING status");
		}

		orderDetail.setStatus(OrderDetailStatus.COMPLETED);
		orderDetailRepository.save(orderDetail);

		Room room = roomRepository.findById(orderDetail.getRoomId())
				.orElseThrow(() -> new RuntimeException("Room not found"));
		room.setStatus(RoomStatus.AVAILABLE);
		roomRepository.save(room);
		return convertToResponse(orderDetail);
	}

	private OrderDetailResponse convertToResponse(OrderDetail orderDetail) {
		OrderDetailResponse response = new OrderDetailResponse();
		response.setOrderDetailId(orderDetail.getOrderDetailId());
		response.setOrderId(orderDetail.getOrderId());
		response.setUserId(orderDetail.getUserId());
		response.setRoomId(orderDetail.getRoomId());
		response.setFloorId(orderDetail.getFloorId());
		response.setStartDate(orderDetail.getStartDate());
		response.setEndDate(orderDetail.getEndDate());
		response.setOrderDescription(orderDetail.getOrderDescription());
		response.setCheckIn(orderDetail.getCheckIn());
		response.setCheckOut(orderDetail.getCheckOut());
		response.setStatus(orderDetail.getStatus());

		// Get room info
		Room room = roomRepository.findById(orderDetail.getRoomId()).orElse(null);
		if (room != null) {
			response.setRoomNumber(room.getRoomNumber());
			response.setRoomType(room.getRoomType());
			response.setRoomStatus(room.getStatus());
		}

		// Get customer info
		if (orderDetail.getUserId() != null) {
			User user = userRepository.findById(orderDetail.getUserId()).orElse(null);
			if (user != null) {
				response.setCustomerName(user.getFirstName() + " " + user.getLastName());
				response.setCustomerPhone(user.getPhone());
			}
		}

		return response;
	}
}
