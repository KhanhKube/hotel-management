package hotel.service.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.entity.*;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckingServiceImpl implements CheckingService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String ROOM_NOT_FOUND = "Phòng không tồn tại";

    // ===== CHECK-IN METHODS =====

    @Override
    @Transactional
    public CheckInResponseDto processCheckIn(CheckInRequestDto request) {
        // Validate room availability
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException(ROOM_NOT_FOUND));
        
        if (!RoomStatus.AVAILABLE.equals(room.getStatus())) {
            throw new RuntimeException("Phòng không sẵn sàng để check-in");
        }

        // Validate customer
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Create or find Order
        Order order = createOrUpdateOrder(customer.getUserId(), room.getFloorId(), 
                                         request.getCheckInDate(), request.getExpectedCheckOutDate());

        // Create OrderDetail for check-in
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(order.getOrderId());
        orderDetail.setUserId(customer.getUserId());
        orderDetail.setRoomId(request.getRoomId());
        orderDetail.setFloorId(room.getFloorId());
        orderDetail.setStartDate(LocalDateTime.now());
        orderDetail.setEndDate(request.getExpectedCheckOutDate().atStartOfDay());
        orderDetail.setCheckIn(LocalDateTime.now());
        orderDetail.setOrderDescription("Check-in: " + request.getNotes());
        orderDetail.setStatus(STATUS_CHECKED_IN);
        
        orderDetailRepository.save(orderDetail);

        // Update room status to OCCUPIED
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        // Build response
        return buildCheckInResponse(orderDetail.getOrderDetailId(), room, customer, request);
    }

    private Order createOrUpdateOrder(Integer userId, Integer floorId, 
                                      LocalDate checkInDate, LocalDate checkOutDate) {
        List<Order> existingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .filter(o -> STATUS_PENDING.equals(o.getStatus()) || STATUS_CHECKED_IN.equals(o.getStatus()))
                .toList();

        Order order;
        if (existingOrders.isEmpty()) {
            order = new Order();
            order.setUserId(userId);
            order.setFloorId(floorId);
            order.setCheckIn(checkInDate.atStartOfDay());
            order.setCheckOut(checkOutDate.atStartOfDay());
            order.setStatus(STATUS_CHECKED_IN);
            orderRepository.save(order);
        } else {
            order = existingOrders.get(0);
            order.setCheckIn(checkInDate.atStartOfDay());
            order.setCheckOut(checkOutDate.atStartOfDay());
            order.setStatus(STATUS_CHECKED_IN);
            orderRepository.save(order);
        }
        
        return order;
    }

    @Override
    public CheckInResponseDto getCheckInById(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException(ROOM_NOT_FOUND));
        
        User customer = userRepository.findById(orderDetail.getUserId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        return CheckInResponseDto.builder()
                .bookingId(orderDetail.getOrderDetailId())
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .customerId(customer.getUserId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerPhone(customer.getPhone())
                .customerEmail(customer.getEmail())
                .checkInDate(orderDetail.getStartDate() != null ? 
                            orderDetail.getStartDate().toLocalDate() : null)
                .expectedCheckOutDate(orderDetail.getEndDate() != null ? 
                                     orderDetail.getEndDate().toLocalDate() : null)
                .numberOfGuests(1) // Default value
                .notes(orderDetail.getOrderDescription())
                .status(orderDetail.getStatus())
                .createdAt(orderDetail.getCreatedAt())
                .build();
    }

    @Override
    public List<CheckInResponseDto> getAllActiveCheckIns() {
        List<OrderDetail> checkIns = orderDetailRepository.findAll().stream()
                .filter(od -> STATUS_CHECKED_IN.equals(od.getStatus()))
                .toList();

        return checkIns.stream()
                .map(this::convertToCheckInResponse)
                .toList();
    }

    // ===== CHECK-OUT METHODS =====

    @Override
    @Transactional
    public void processCheckOut(CheckOutRequestDto request) {
        OrderDetail orderDetail = orderDetailRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!STATUS_CHECKED_IN.equals(orderDetail.getStatus())) {
            throw new RuntimeException("Chỉ có thể check-out booking đang ở trạng thái CHECKED_IN");
        }

        // Update order detail status
        orderDetail.setStatus(STATUS_CHECKED_OUT);
        orderDetail.setCheckOut(LocalDateTime.now());
        if (request.getNotes() != null) {
            orderDetail.setOrderDescription(orderDetail.getOrderDescription() + 
                " | Check-out notes: " + request.getNotes());
        }
        orderDetailRepository.save(orderDetail);

        // Update room status to available
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        // Update order status
        List<OrderDetail> remainingCheckIns = orderDetailRepository.findAll().stream()
                .filter(od -> od.getOrderId().equals(orderDetail.getOrderId()))
                .filter(od -> STATUS_CHECKED_IN.equals(od.getStatus()))
                .toList();

        if (remainingCheckIns.isEmpty()) {
            Order order = orderRepository.findById(orderDetail.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
            order.setStatus(STATUS_CHECKED_OUT);
            orderRepository.save(order);
        }
    }

    @Override
    public CheckInResponseDto getCheckOutById(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        
        if (!STATUS_CHECKED_OUT.equals(orderDetail.getStatus())) {
            throw new RuntimeException("Booking chưa được check-out");
        }
        
        return convertToCheckInResponse(orderDetail);
    }

    @Override
    public List<CheckInResponseDto> getCheckOutCandidates() {
        List<OrderDetail> candidates = orderDetailRepository.findAll().stream()
                .filter(od -> STATUS_CHECKED_IN.equals(od.getStatus()))
                .toList();

        return candidates.stream()
                .map(this::convertToCheckInResponse)
                .toList();
    }

    @Override
    public List<CheckInResponseDto> getAllCheckOutHistory() {
        List<OrderDetail> checkOuts = orderDetailRepository.findAll().stream()
                .filter(od -> STATUS_CHECKED_OUT.equals(od.getStatus()))
                .toList();

        return checkOuts.stream()
                .map(this::convertToCheckInResponse)
                .toList();
    }

    // ===== UTILITY METHODS =====

    @Override
    public List<Room> getAvailableRooms() {
        return roomRepository.findAll().stream()
                .filter(r -> RoomStatus.AVAILABLE.equals(r.getStatus()))
                .filter(r -> !r.getIsDeleted())
                .toList();
    }

    @Override
    public List<User> getAllCustomers() {
        return userRepository.findByRole("CUSTOMER").stream()
                .filter(u -> !u.getIsDeleted())
                .toList();
    }

    // ===== HELPER METHODS =====

    private CheckInResponseDto convertToCheckInResponse(OrderDetail orderDetail) {
        Room room = roomRepository.findById(orderDetail.getRoomId()).orElse(null);
        User customer = userRepository.findById(orderDetail.getUserId()).orElse(null);

        if (room == null || customer == null) {
            return null;
        }

        return CheckInResponseDto.builder()
                .bookingId(orderDetail.getOrderDetailId())
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .customerId(customer.getUserId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerPhone(customer.getPhone())
                .customerEmail(customer.getEmail())
                .checkInDate(orderDetail.getCheckIn() != null ? 
                            orderDetail.getCheckIn().toLocalDate() : null)
                .expectedCheckOutDate(orderDetail.getEndDate() != null ? 
                                     orderDetail.getEndDate().toLocalDate() : null)
                .numberOfGuests(1) // Default value
                .notes(orderDetail.getOrderDescription())
                .status(orderDetail.getStatus())
                .createdAt(orderDetail.getCreatedAt())
                .build();
    }

    private CheckInResponseDto buildCheckInResponse(Integer orderDetailId, Room room, User customer, 
                                                    CheckInRequestDto request) {
        return CheckInResponseDto.builder()
                .bookingId(orderDetailId)
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .customerId(customer.getUserId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerPhone(customer.getPhone())
                .customerEmail(customer.getEmail())
                .checkInDate(request.getCheckInDate())
                .expectedCheckOutDate(request.getExpectedCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests())
                .notes(request.getNotes())
                .services(request.getServices())
                .depositAmount(request.getDepositAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(STATUS_CHECKED_IN)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

