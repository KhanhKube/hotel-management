package hotel.service.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.dto.checking.CheckOutResponseDto;
import hotel.db.entity.Order;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckServiceImpl implements CheckService {

    private static final String CHECKED_IN_STATUS = "CHECKED_IN";
    private static final String CHECKED_OUT_STATUS = "CHECKED_OUT";
    private static final String SYSTEM_USER = "SYSTEM";

    private final OrderRepository orderRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CheckInResponseDto> getAllActiveCheckIns() {
        log.info("Getting all active check-ins");
        List<Order> orders = orderRepository.findByStatus(CHECKED_IN_STATUS);
        return orders.stream()
                .map(this::convertToCheckInResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckOutCandidates() {
        log.info("Getting check-out candidates");
        List<Order> orders = orderRepository.findByStatus(CHECKED_IN_STATUS);
        return orders.stream()
                .map(this::convertToCheckOutResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInResponseDto getBookingById(Integer bookingId) {
        log.info("Getting booking by ID: {}", bookingId);
        Order order = orderRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
        return convertToCheckInResponseDto(order);
    }

    @Override
    public CheckInResponseDto checkIn(CheckInRequestDto checkInRequestDto) {
        log.info("Processing check-in for room: {}", checkInRequestDto.getRoomId());
        
        // Kiểm tra khách hàng
        userRepository.findById(checkInRequestDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Tạo booking mới
        Order order = new Order();
        order.setUserId(checkInRequestDto.getCustomerId());
        order.setRoomId(checkInRequestDto.getRoomId());
        order.setCheckIn(checkInRequestDto.getCheckInDate().atStartOfDay());
        order.setCheckOut(checkInRequestDto.getExpectedCheckOutDate().atStartOfDay());
        order.setStatus(CHECKED_IN_STATUS);

        Order savedOrder = orderRepository.save(order);

        log.info("Check-in successful for booking ID: {}", savedOrder.getOrderId());
        return convertToCheckInResponseDto(savedOrder);
    }

    @Override
    public CheckOutResponseDto checkOut(CheckOutRequestDto checkOutRequestDto) {
        log.info("Processing check-out for booking: {}", checkOutRequestDto.getBookingId());
        
        Order order = orderRepository.findById(checkOutRequestDto.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!CHECKED_IN_STATUS.equals(order.getStatus())) {
            throw new IllegalArgumentException("Booking is not in CHECKED_IN status");
        }

        // Cập nhật thông tin check-out
        order.setCheckOut(checkOutRequestDto.getActualCheckOutDate().atStartOfDay());
        order.setStatus(CHECKED_OUT_STATUS);

        Order savedOrder = orderRepository.save(order);

        log.info("Check-out successful for booking ID: {}", savedOrder.getOrderId());
        return convertToCheckOutResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> getAvailableRooms() {
        log.info("Getting available rooms");
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .toList();
        return rooms.stream()
                .map(room -> {
                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("roomId", room.getRoomId());
                    roomInfo.put("roomNumber", room.getRoomNumber());
                    roomInfo.put("roomType", room.getRoomType());
                    roomInfo.put("price", room.getPrice());
                    return (Object) roomInfo;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> getCustomers() {
        log.info("Getting customers");
        List<User> customers = userRepository.findByRole("CUSTOMER");
        return customers.stream()
                .map(customer -> {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("userId", customer.getUserId());
                    customerInfo.put("fullName", customer.getFirstName() + " " + customer.getLastName());
                    customerInfo.put("email", customer.getEmail());
                    customerInfo.put("phone", customer.getPhone());
                    return (Object) customerInfo;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Integer roomId, String checkInDate, String checkOutDate) {
        log.info("Checking room availability for room: {}", roomId);
        Room room = roomRepository.findById(roomId).orElse(null);
        return room != null && room.getStatus() == RoomStatus.AVAILABLE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckInOutHistory() {
        log.info("Getting check-in/out history");
        List<Order> orders = orderRepository.findByStatusIn(List.of(CHECKED_OUT_STATUS));
        return orders.stream()
                .map(this::convertToCheckOutResponseDto)
                .toList();
    }

    private CheckInResponseDto convertToCheckInResponseDto(Order order) {
        User customer = userRepository.findById(order.getUserId()).orElse(null);
        
        return CheckInResponseDto.builder()
                .bookingId(order.getOrderId())
                .roomId(order.getRoomId())
                .roomNumber(order.getRoomId() != null ? "Phòng " + order.getRoomId() : "Chưa chọn phòng")
                .roomType(order.getRoomId() != null ? "Deluxe" : null)
                .customerId(order.getUserId())
                .customerName(customer != null ? customer.getFirstName() + " " + customer.getLastName() : null)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .checkInDate(order.getCheckIn() != null ? order.getCheckIn().toLocalDate() : null)
                .expectedCheckOutDate(order.getCheckOut() != null ? order.getCheckOut().toLocalDate() : null)
                .numberOfGuests(null)
                .notes(null)
                .services(null)
                .depositAmount(null)
                .paymentMethod(null)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .createdBy(null)
                .build();
    }

    private CheckOutResponseDto convertToCheckOutResponseDto(Order order) {
        User customer = userRepository.findById(order.getUserId()).orElse(null);
        
        return CheckOutResponseDto.builder()
                .bookingId(order.getOrderId())
                .roomId(order.getRoomId())
                .roomNumber(order.getRoomId() != null ? "Phòng " + order.getRoomId() : "Chưa chọn phòng")
                .roomType(order.getRoomId() != null ? "Deluxe" : null)
                .customerId(order.getUserId())
                .customerName(customer != null ? customer.getFirstName() + " " + customer.getLastName() : null)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .checkInDate(order.getCheckIn() != null ? order.getCheckIn().toLocalDate() : null)
                .expectedCheckOutDate(order.getCheckOut() != null ? order.getCheckOut().toLocalDate() : null)
                .actualCheckOutDate(null)
                .numberOfGuests(null)
                .totalAmount(null)
                .penaltyAmount(null)
                .penaltyReason(null)
                .notes(null)
                .paymentMethod(null)
                .roomCondition(null)
                .hasDamage(null)
                .damageDescription(null)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .createdBy(null)
                .updatedBy(null)
                .build();
    }

    @Override
    public CheckInResponseDto getCheckInById(Integer bookingId) {
        log.info("Getting check-in details for booking ID: {}", bookingId);
        
        Order order = orderRepository.findById(bookingId).orElse(null);
        if (order == null) {
            return null;
        }
        
        return convertToCheckInResponseDto(order);
    }
}