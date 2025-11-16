package hotel.service.checking;

import hotel.db.dto.checking.*;
import hotel.db.entity.Furnishing;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.entity.RoomFurnishing;
import hotel.db.entity.User;
import hotel.db.enums.OrderDetailStatus;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.furnishing.FurnishingRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomfurnishing.RoomFurnishingRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckingServiceImpl implements CheckingService {

    private final OrderDetailRepository orderDetailRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomFurnishingRepository roomFurnishingRepository;
    private final FurnishingRepository furnishingRepository;

    @Override
    public Page<OrderDetailResponse> getReservedOrders(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<OrderDetail> reservedOrders = orderDetailRepository.findByStatusOrderByCreatedAtDesc(
                OrderDetailStatus.RESERVED, pageable);

        List<OrderDetail> validOrders = new ArrayList<>();

        for (OrderDetail order : reservedOrders.getContent()) {
            if (isOrderValid(order, now)) {
                validOrders.add(order);
            } else {
                markOrderAsExpired(order);
            }
        }

        List<OrderDetailResponse> responses = validOrders.stream()
                .map(this::convertToResponse)
                .toList();

        return new PageImpl<>(responses, pageable, validOrders.size());
    }

    private boolean isOrderValid(OrderDetail order, LocalDateTime now) {
        if (order.getStartDate() == null || order.getEndDate() == null) {
            return false;
        }
        // Điều kiện: checkInDate <= ngày hiện tại < checkOutDate
        return (order.getStartDate().isBefore(now) || order.getStartDate().isEqual(now)) 
                && now.isBefore(order.getEndDate());
    }

    private void markOrderAsExpired(OrderDetail order) {
        order.setIsDeleted(true);
        orderDetailRepository.save(order);
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

        // BẮT BUỘC phải kiểm tra dụng cụ phòng trước khi check-out
        if (request.getFurnishingCheckCompleted() == null || !request.getFurnishingCheckCompleted()) {
            throw new RuntimeException("Phải kiểm tra dụng cụ phòng trước khi check-out");
        }

        // Nhân viên xác nhận check-out (sau khi đã kiểm tra dụng cụ)
        orderDetail.setStatus(OrderDetailStatus.CHECKED_OUT);
        orderDetail.setCheckOut(LocalDateTime.now());
        
        // Lưu ghi chú kiểm tra dụng cụ vào field riêng (không bắt buộc)
        if (request.getIssueNote() != null && !request.getIssueNote().trim().isEmpty()) {
            orderDetail.setFurnishingCheckNote(request.getIssueNote());
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
        response.setFurnishingCheckNote(orderDetail.getFurnishingCheckNote());
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

    @Override
    public List<Map<String, Object>> getRoomFurnishings(Integer orderDetailId) {
        // Lấy thông tin order detail để biết roomId
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Order detail not found"));
        
        Integer roomId = orderDetail.getRoomId();
        
        // Lấy danh sách furnishing của phòng từ room_furnishing table
        List<RoomFurnishing> roomFurnishings = roomFurnishingRepository.findByRoomIdAndIsDeletedFalse(roomId);
        
        List<Map<String, Object>> furnishings = new ArrayList<>();
        
        for (RoomFurnishing rf : roomFurnishings) {
            // Lấy thông tin chi tiết của furnishing
            Furnishing furnishing = furnishingRepository.findById(rf.getFurnishingId()).orElse(null);
            
            if (furnishing != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("furnishingId", furnishing.getFurnishingId());
                item.put("name", furnishing.getName());
                item.put("description", furnishing.getFurnishingDescription());
                item.put("quantity", rf.getQuantity());
                furnishings.add(item);
            }
        }
        
        return furnishings;
    }
}

