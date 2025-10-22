package hotel.service.checkincheckout;

import hotel.db.entity.Order;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckInCheckOutService {

    // Constants
    private static final String AVAILABLE_STATUS = "AVAILABLE";
    private static final String OCCUPIED_STATUS = "OCCUPIED";
    private static final String CHECKED_IN_STATUS = "CHECKED_IN";
    private static final String CHECKED_OUT_STATUS = "CHECKED_OUT";
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final OrderRepository orderRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách phòng có sẵn để check-in
     */
    @Transactional(readOnly = true)
    public List<Room> getAvailableRooms() {
        log.info("Getting available rooms for check-in");
        return roomRepository.findAll().stream()
                .filter(room -> AVAILABLE_STATUS.equals(room.getStatus()))
                .toList();
    }

    /**
     * Lấy danh sách khách hàng (User với role CUSTOMER và chưa bị xóa)
     */
    @Transactional(readOnly = true)
    public List<User> getCustomers() {
        log.info("Getting customers for check-in");
        return userRepository.findAll().stream()
                .filter(user -> CUSTOMER_ROLE.equals(user.getRole()) && !user.getIsDeleted())
                .toList();
    }

    /**
     * Thực hiện check-in
     */
    public Order processCheckIn(Integer roomId, Integer customerId, String notes) {
        log.info("Processing check-in for room: {} and customer: {}", roomId, customerId);
        
        try {
            // Kiểm tra phòng có sẵn không
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
            
            if (!"AVAILABLE".equals(room.getStatus())) {
                throw new IllegalArgumentException("Phòng không khả dụng");
            }
            
            // Kiểm tra khách hàng
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại"));
            
            // Tạo Order mới (check-in)
            Order order = new Order();
            order.setUserId(customerId);
            order.setFloorId(room.getFloorId()); // Sử dụng floorId từ Room, không phải roomId
            order.setCheckIn(LocalDateTime.now());
            order.setCheckOut(LocalDateTime.now().plusDays(1)); // Set checkOut mặc định là ngày mai
            order.setStatus("CHECKED_IN");
            
            Order savedOrder = orderRepository.save(order);
            
            // Cập nhật trạng thái phòng
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
            
            log.info("Check-in completed for order: {}", savedOrder.getOrderId());
            return savedOrder;
            
        } catch (Exception e) {
            log.error("Error processing check-in: {}", e.getMessage());
            throw new IllegalArgumentException("Lỗi khi check-in: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách Order đang check-in (để check-out)
     */
    @Transactional(readOnly = true)
    public List<Order> getActiveOrders() {
        log.info("Getting active orders for check-out");
        return orderRepository.findAll().stream()
                .filter(order -> "CHECKED_IN".equals(order.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết Order theo ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Integer orderId) {
        log.info("Getting order details for ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order không tồn tại"));
    }

    /**
     * Thực hiện check-out
     */
    public Order processCheckOut(Integer orderId, String notes) {
        log.info("Processing check-out for order: {}", orderId);
        
        try {
            // Lấy Order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order không tồn tại"));
            
            if (!"CHECKED_IN".equals(order.getStatus())) {
                throw new IllegalArgumentException("Order không thể check-out. Trạng thái hiện tại: " + order.getStatus());
            }
            
            // Cập nhật Order
            order.setStatus("CHECKED_OUT");
            order.setCheckOut(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            
        // Tìm phòng theo floorId và cập nhật thành AVAILABLE
        // Note: Cần tìm room theo floorId, không phải trực tiếp từ order.getFloorId()
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(r -> r.getFloorId().equals(order.getFloorId()))
                .toList();
        
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy phòng với floorId: " + order.getFloorId());
        }
        
        Room room = rooms.get(0); // Lấy phòng đầu tiên
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
            
            log.info("Check-out completed for order: {}", updatedOrder.getOrderId());
            return updatedOrder;
            
        } catch (Exception e) {
            log.error("Error processing check-out: {}", e.getMessage());
            throw new IllegalArgumentException("Lỗi khi check-out: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin phòng từ Order
     */
    @Transactional(readOnly = true)
    public Room getRoomFromOrder(Order order) {
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(r -> r.getFloorId().equals(order.getFloorId()))
                .toList();
        
        return rooms.isEmpty() ? null : rooms.get(0);
    }

    /**
     * Lấy thông tin khách hàng từ Order (chỉ lấy user chưa bị xóa)
     */
    @Transactional(readOnly = true)
    public User getCustomerFromOrder(Order order) {
        User user = userRepository.findById(order.getUserId()).orElse(null);
        return (user != null && !user.getIsDeleted()) ? user : null;
    }
}
