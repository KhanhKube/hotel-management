package hotel.service.checking;

import hotel.db.dto.checking.*;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.enums.OrderDetailStatus;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckingServiceImpl implements CheckingService {

    private final OrderDetailRepository orderDetailRepository;
    private final RoomRepository roomRepository;
    private final hotel.db.repository.user.UserRepository userRepository;

    @Override
    public Page<OrderDetailResponse> getReservedOrders(Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
                OrderDetailStatus.RESERVED, pageable);
        return orderDetails.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public OrderDetailResponse startCheckIn(CheckInRequest request) {
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("Order detail not found"));

        if (!OrderDetailStatus.RESERVED.equals(orderDetail.getStatus())) {
            throw new RuntimeException("Order detail must be in RESERVED status");
        }

        // Chỉ update OrderDetail, KHÔNG update Room
        orderDetail.setStatus(OrderDetailStatus.CHECKING_IN);
        orderDetailRepository.save(orderDetail);

        log.info("Started check-in for order detail: {}", orderDetail.getOrderDetailId());
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
            // Khách xác nhận: chỉ update OrderDetail
            orderDetail.setStatus(OrderDetailStatus.CUSTOMER_CONFIRM);
            orderDetailRepository.save(orderDetail);
            log.info("Customer confirmed check-in for order detail: {}", orderDetail.getOrderDetailId());
        } else if ("STAFF".equals(request.getConfirmedBy())) {
            // Staff xác nhận: phải có customer confirm trước
            if (OrderDetailStatus.CUSTOMER_CONFIRM.equals(orderDetail.getStatus())) {
                orderDetail.setStatus(OrderDetailStatus.OCCUPIED);
                orderDetail.setCheckIn(LocalDateTime.now()); // Set thời gian check-in thực tế
                orderDetailRepository.save(orderDetail);

                // Update Room status
                Room room = roomRepository.findById(orderDetail.getRoomId())
                        .orElseThrow(() -> new RuntimeException("Room not found"));
                room.setStatus(RoomStatus.OCCUPIED);
                roomRepository.save(room);

                log.info("Completed check-in for order detail: {}", orderDetail.getOrderDetailId());
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

        // Chỉ update OrderDetail, Room vẫn OCCUPIED
        orderDetail.setStatus(OrderDetailStatus.NEED_CHECKOUT);
        orderDetailRepository.save(orderDetail);

        log.info("Started check-out for order detail: {}", orderDetail.getOrderDetailId());
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

        // KHÔNG update Room - Room vẫn OCCUPIED cho đến khi lễ tân confirm
        log.info("Staff completed check-out for order detail: {}", orderDetail.getOrderDetailId());
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

        // Cập nhật trạng thái phòng
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setStatus(RoomStatus.NEED_CLEAN);
        roomRepository.save(room);

        log.info("Receptionist confirmed check-out for order detail: {}", orderDetail.getOrderDetailId());
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

        // Cập nhật trạng thái phòng
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        log.info("Started cleaning for order detail: {}", orderDetail.getOrderDetailId());
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

        // Cập nhật trạng thái phòng về AVAILABLE
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        log.info("Completed cleaning for order detail: {}", orderDetail.getOrderDetailId());
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
            hotel.db.entity.User user = userRepository.findById(orderDetail.getUserId()).orElse(null);
            if (user != null) {
                response.setCustomerName(user.getFirstName() + " " + user.getLastName());
                response.setCustomerPhone(user.getPhone());
            }
        }

        return response;
    }
}
