package hotel.service.checking;

import hotel.db.dto.checking.AfterCheckOutRequestDto;
import hotel.db.dto.checking.BookingDto;
import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.db.enums.RoomStatus;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation xử lý check-in, check-out và after check-out
 * Logic đơn giản, rõ ràng, tối ưu performance - tránh N+1 query
 */
@Service
@RequiredArgsConstructor
public class CheckingServiceImpl implements CheckingService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // Các trạng thái
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CART = "CART";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    private static final String STATUS_COMPLETED = "COMPLETED";

    // ===== CHECK-IN =====

    @Override
    @Transactional
    public void checkIn(CheckInRequestDto request) {
        // Lấy booking
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // Kiểm tra status: chỉ check-in được PENDING hoặc CART
        String currentStatus = orderDetail.getStatus();
        if (!STATUS_PENDING.equals(currentStatus) && !STATUS_CART.equals(currentStatus)) {
            throw new RuntimeException("Chỉ có thể check-in booking ở trạng thái PENDING hoặc CART");
        }

        // Lấy phòng
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Kiểm tra phòng: cho phép check-in nếu phòng ở trạng thái AVAILABLE hoặc RESERVED
        // - AVAILABLE: phòng trống, có thể check-in ngay
        // - RESERVED: phòng đã được đặt (PENDING/CART), có thể check-in
        String roomStatus = room.getStatus();
        if (!RoomStatus.AVAILABLE.equals(roomStatus) && !RoomStatus.RESERVED.equals(roomStatus)) {
            throw new RuntimeException("Phòng không sẵn sàng để check-in. Trạng thái hiện tại: " + roomStatus);
        }

        // Cập nhật check-in
        orderDetail.setCheckIn(LocalDateTime.now());
        orderDetail.setStatus(STATUS_CHECKED_IN);
        orderDetailRepository.save(orderDetail);

        // Cập nhật phòng → OCCUPIED
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        // Cập nhật Order
        Order order = orderRepository.findById(orderDetail.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
        order.setStatus(STATUS_CHECKED_IN);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getCheckInList(Pageable pageable) {
        // Query với pagination ở database level
        Page<OrderDetail> orderDetailPage = orderDetailRepository.findCheckInList(pageable);

        // Batch load Room và User để tránh N+1 query (NHANH HƠN)
        List<OrderDetail> orderDetails = orderDetailPage.getContent();
        if (orderDetails.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Lấy tất cả roomIds và userIds
        Set<Integer> roomIds = orderDetails.stream()
                .map(OrderDetail::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<Integer> userIds = orderDetails.stream()
                .map(OrderDetail::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch load tất cả Room và User (CHỈ 2 QUERIES THAY VÌ N*2 QUERIES)
        Map<Integer, Room> roomMap = roomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(Room::getRoomId, room -> room));
        
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // Convert sang DTO sử dụng Map (NHANH)
        List<BookingDto> dtos = orderDetails.stream()
                .map(od -> convertToBookingDtoWithMaps(od, roomMap, userMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, orderDetailPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long countCheckInItems() {
        // Dùng repository method với count ở database level (NHANH HƠN)
        return orderDetailRepository.countByStatusIn(Arrays.asList(STATUS_PENDING, STATUS_CART, STATUS_CHECKED_IN));
    }

    // ===== CHECK-OUT =====

    @Override
    @Transactional
    public void checkOut(CheckOutRequestDto request) {
        // Lấy booking
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // Kiểm tra status: chỉ check-out được CHECKED_IN
        if (!STATUS_CHECKED_IN.equals(orderDetail.getStatus())) {
            throw new RuntimeException("Chỉ có thể check-out booking ở trạng thái CHECKED_IN");
        }

        // Lấy phòng
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Cập nhật check-out
        orderDetail.setCheckOut(LocalDateTime.now());
        orderDetail.setStatus(STATUS_CHECKED_OUT);
        orderDetailRepository.save(orderDetail);

        // Cập nhật phòng → CLEANING
        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        // Cập nhật Order
        Order order = orderRepository.findById(orderDetail.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
        order.setStatus(STATUS_CHECKED_OUT);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getCheckOutList(Pageable pageable) {
        // Query với pagination ở database level
        Page<OrderDetail> orderDetailPage = orderDetailRepository.findByStatusOrderByCreatedAtDesc(STATUS_CHECKED_IN, pageable);

        // Batch load Room và User để tránh N+1 query (NHANH HƠN)
        List<OrderDetail> orderDetails = orderDetailPage.getContent();
        if (orderDetails.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Lấy tất cả roomIds và userIds
        Set<Integer> roomIds = orderDetails.stream()
                .map(OrderDetail::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<Integer> userIds = orderDetails.stream()
                .map(OrderDetail::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch load tất cả Room và User (CHỈ 2 QUERIES THAY VÌ N*2 QUERIES)
        Map<Integer, Room> roomMap = roomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(Room::getRoomId, room -> room));
        
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // Convert sang DTO sử dụng Map (NHANH)
        List<BookingDto> dtos = orderDetails.stream()
                .map(od -> convertToBookingDtoWithMaps(od, roomMap, userMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, orderDetailPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long countCheckOutItems() {
        // Dùng repository method với count ở database level (NHANH HƠN)
        return orderDetailRepository.countByStatus(STATUS_CHECKED_IN);
    }

    // ===== AFTER CHECK-OUT =====

    @Override
    @Transactional
    public void afterCheckOut(AfterCheckOutRequestDto request) {
        // Lấy booking
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // Kiểm tra status: chỉ xử lý được CHECKED_OUT
        if (!STATUS_CHECKED_OUT.equals(orderDetail.getStatus())) {
            throw new RuntimeException("Chỉ có thể xử lý after check-out cho booking ở trạng thái CHECKED_OUT");
        }

        // Lấy phòng
        Room room = roomRepository.findById(orderDetail.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Kiểm tra phòng phải CLEANING
        if (!RoomStatus.CLEANING.equals(room.getStatus())) {
            throw new RuntimeException("Phòng không ở trạng thái cần dọn dẹp. Trạng thái: " + room.getStatus());
        }

        // Cập nhật booking → COMPLETED
        orderDetail.setStatus(STATUS_COMPLETED);
        orderDetailRepository.save(orderDetail);

        // Cập nhật phòng
        if (request.getReadyForNextGuest()) {
            // Phòng sẵn sàng → AVAILABLE
            room.setStatus(RoomStatus.AVAILABLE);
        } else {
            // Phòng cần bảo trì → MAINTENANCE
            room.setStatus(RoomStatus.MAINTENANCE);
        }
        roomRepository.save(room);

        // Cập nhật Order
        Order order = orderRepository.findById(orderDetail.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
        order.setStatus(STATUS_COMPLETED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getAfterCheckOutList(Pageable pageable) {
        // Query với pagination ở database level
        Page<Room> cleaningRoomsPage = roomRepository.findByStatusAndIsDeletedFalse(RoomStatus.CLEANING, pageable);

        // Batch load OrderDetail và User để tránh N+1 query (NHANH HƠN)
        List<Room> cleaningRooms = cleaningRoomsPage.getContent();
        if (cleaningRooms.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Lấy tất cả roomIds
        List<Integer> roomIds = cleaningRooms.stream()
                .map(Room::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Batch load OrderDetail cho tất cả rooms (TỐI ƯU: query 1 lần với IN clause)
        List<OrderDetail> allCheckedOut = orderDetailRepository.findCheckedOutByRoomIds(roomIds);

        // Group OrderDetail theo roomId, lấy cái mới nhất
        Map<Integer, OrderDetail> latestOrderDetailMap = allCheckedOut.stream()
                .collect(Collectors.toMap(
                        OrderDetail::getRoomId,
                        od -> od,
                        (od1, od2) -> {
                            LocalDateTime checkOut1 = od1.getCheckOut() != null ? od1.getCheckOut() : LocalDateTime.MIN;
                            LocalDateTime checkOut2 = od2.getCheckOut() != null ? od2.getCheckOut() : LocalDateTime.MIN;
                            return checkOut1.compareTo(checkOut2) > 0 ? od1 : od2;
                        }
                ));

        // Lấy tất cả userIds
        Set<Integer> userIds = latestOrderDetailMap.values().stream()
                .map(OrderDetail::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch load tất cả User (CHỈ 1 QUERY)
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // Convert sang DTO sử dụng Map (NHANH)
        List<BookingDto> dtos = cleaningRooms.stream()
                .map(room -> {
                    OrderDetail orderDetail = latestOrderDetailMap.get(room.getRoomId());
                    if (orderDetail == null) {
                        return null;
                    }
                    return convertToBookingDtoWithMaps(orderDetail, Map.of(room.getRoomId(), room), userMap);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, cleaningRoomsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAfterCheckOutItems() {
        // Dùng repository method với count ở database level (NHANH HƠN)
        return roomRepository.countByStatusAndIsDeletedFalse(RoomStatus.CLEANING);
    }

    // ===== HELPER METHODS =====

    /**
     * Convert OrderDetail sang BookingDto với Map (TRÁNH N+1 QUERY)
     */
    private BookingDto convertToBookingDtoWithMaps(OrderDetail orderDetail, 
                                                   Map<Integer, Room> roomMap, 
                                                   Map<Integer, User> userMap) {
        Room room = roomMap.get(orderDetail.getRoomId());
        User customer = userMap.get(orderDetail.getUserId());

        if (room == null || customer == null) {
            return null;
        }

        return BookingDto.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
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
                .actualCheckOutDate(orderDetail.getCheckOut() != null ? 
                                   orderDetail.getCheckOut().toLocalDate() : null)
                .status(orderDetail.getStatus())
                .roomStatus(room.getStatus())
                .createdAt(orderDetail.getCreatedAt())
                .build();
    }
}
