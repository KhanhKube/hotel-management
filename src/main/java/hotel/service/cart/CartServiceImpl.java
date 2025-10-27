package hotel.service.cart;

import hotel.db.dto.cart.AddToCartRequest;
import hotel.db.dto.cart.CartItemDto;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    
    // Temporary storage for cart items (in production, use Redis or Database)
    private final Map<Integer, List<CartItemDto>> cartStorage = new ConcurrentHashMap<>();
    
    @Override
    public void addToCart(Integer userId, AddToCartRequest request) {
        // Validate request
        if (request.getRoomId() == null) {
            throw new RuntimeException("Room ID is required");
        }
        if (request.getCheckIn() == null) {
            throw new RuntimeException("Check-in date is required");
        }
        if (request.getCheckOut() == null) {
            throw new RuntimeException("Check-out date is required");
        }
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + request.getRoomId()));
        
        // Calculate number of days
        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (days <= 0) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        
        // Calculate total price
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));
        
        // Get room image from room_images table
        String imageUrl = null;
        List<RoomImage> roomImages = roomImageRepository.findByRoomIdAndIsDeletedFalse(room.getRoomId());
        if (!roomImages.isEmpty()) {
            imageUrl = roomImages.get(0).getRoomImageUrl();
        }
        
        // Create cart item
        CartItemDto cartItem = new CartItemDto();
        cartItem.setRoomId(room.getRoomId());
        cartItem.setRoomType(room.getRoomType());
        cartItem.setRoomNumber(room.getRoomNumber());
        cartItem.setPrice(room.getPrice());
        cartItem.setCheckIn(request.getCheckIn());
        cartItem.setCheckOut(request.getCheckOut());
        cartItem.setNumberOfDays((int) days);
        cartItem.setTotalPrice(totalPrice);
        cartItem.setImageRoom(imageUrl);
        
        // Add to cart
        cartStorage.computeIfAbsent(userId, k -> new ArrayList<>()).add(cartItem);
    }
    
    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        return cartStorage.getOrDefault(userId, new ArrayList<>());
    }
    
    @Override
    public void removeFromCart(Integer userId, Integer roomId) {
        List<CartItemDto> items = cartStorage.get(userId);
        if (items != null) {
            items.removeIf(item -> item.getRoomId().equals(roomId));
        }
    }
    
    @Override
    public void clearCart(Integer userId) {
        cartStorage.remove(userId);
    }
    
    @Override
    public int getCartItemCount(Integer userId) {
        List<CartItemDto> items = cartStorage.get(userId);
        return items != null ? items.size() : 0;
    }

    @Override
    @Transactional
    public List<Integer> checkout(Integer userId) {
        List<CartItemDto> cartItems = getCartItems(userId);
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }
        
        List<Integer> orderIds = new ArrayList<>();
        
        // Create an order for each cart item
        for (CartItemDto item : cartItems) {
            // Get room to get floorId
            Room room = roomRepository.findById(item.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + item.getRoomId()));
            
            // Create Order
            Order order = new Order();
            order.setUserId(userId);
            order.setFloorId(room.getFloorId());
            order.setCheckIn(item.getCheckIn());
            order.setCheckOut(item.getCheckOut());
            order.setStatus("PENDING");
            
            Order savedOrder = orderRepository.save(order);
            orderIds.add(savedOrder.getOrderId());
            
            // Create OrderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(savedOrder.getOrderId());
            orderDetail.setUserId(userId);
            orderDetail.setRoomId(item.getRoomId());
            orderDetail.setFloorId(room.getFloorId());
            orderDetail.setStartDate(item.getCheckIn());
            orderDetail.setEndDate(item.getCheckOut());
            orderDetail.setCheckIn(item.getCheckIn());
            orderDetail.setCheckOut(item.getCheckOut());
            orderDetail.setStatus("PENDING");
            orderDetail.setOrderDescription("Đặt phòng " + item.getRoomType() + " - " + item.getRoomNumber() 
                    + " từ " + item.getCheckIn() + " đến " + item.getCheckOut());
            
            orderDetailRepository.save(orderDetail);
        }
        
        // Clear cart after successful checkout
        clearCart(userId);
        
        return orderIds;
    }
}
