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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final RoomRepository roomRepository;
	private final RoomImageRepository roomImageRepository;
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;

	@Override
	@Transactional
	public void addToCart(Integer userId, AddToCartRequest request) {
		System.out.println("=== Adding to cart for userId: " + userId + " ===");

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

		// Create Order with status CART
		Order order = new Order();
		order.setUserId(userId);
		order.setFloorId(room.getFloorId());
		order.setCheckIn(request.getCheckIn());
		order.setCheckOut(request.getCheckOut());
		order.setStatus("CART"); // Status CART for cart items

		Order savedOrder = orderRepository.save(order);
		System.out.println("Created cart order with ID: " + savedOrder.getOrderId());

		// Create OrderDetail
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.setOrderId(savedOrder.getOrderId());
		orderDetail.setUserId(userId);
		orderDetail.setRoomId(room.getRoomId());
		orderDetail.setFloorId(room.getFloorId());
		orderDetail.setStartDate(request.getCheckIn());
		orderDetail.setEndDate(request.getCheckOut());
		orderDetail.setCheckIn(request.getCheckIn());
		orderDetail.setCheckOut(request.getCheckOut());
		orderDetail.setStatus("CART");
		orderDetail.setOrderDescription("Giỏ hàng - " + room.getRoomType() + " - Phòng " + room.getRoomNumber());

		orderDetailRepository.save(orderDetail);

		System.out.println("Cart item added successfully to database!");
	}

	@Override
	public List<CartItemDto> getCartItems(Integer userId) {
		System.out.println("=== Getting cart items for userId: " + userId + " ===");

		// Get all orders with status CART
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");
		System.out.println("Found " + cartOrders.size() + " CART orders");

		List<CartItemDto> cartItems = new ArrayList<>();

		for (Order order : cartOrders) {
			// Get order details
			List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());

			for (OrderDetail detail : orderDetails) {
				Room room = roomRepository.findById(detail.getRoomId()).orElse(null);
				if (room == null) continue;

				// Calculate days and total price
				long days = ChronoUnit.DAYS.between(detail.getCheckIn(), detail.getCheckOut());
				BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));

				// Get room image
				String imageUrl = null;
				List<RoomImage> roomImages = roomImageRepository.findByRoomIdAndIsDeletedFalse(room.getRoomId());
				if (!roomImages.isEmpty()) {
					imageUrl = roomImages.get(0).getRoomImageUrl();
				}

				// Create CartItemDto
				CartItemDto cartItem = new CartItemDto();
				cartItem.setRoomId(room.getRoomId());
				cartItem.setRoomType(room.getRoomType());
				cartItem.setRoomNumber(room.getRoomNumber());
				cartItem.setPrice(room.getPrice());
				cartItem.setCheckIn(detail.getCheckIn());
				cartItem.setCheckOut(detail.getCheckOut());
				cartItem.setNumberOfDays((int) days);
				cartItem.setTotalPrice(totalPrice);
				cartItem.setImageRoom(imageUrl);

				cartItems.add(cartItem);
			}
		}

		System.out.println("Returning " + cartItems.size() + " cart items");
		return cartItems;
	}

	@Override
	@Transactional
	public void removeFromCart(Integer userId, Integer roomId) {
		System.out.println("=== Removing room " + roomId + " from cart for userId: " + userId + " ===");

		// Find cart orders
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");

		for (Order order : cartOrders) {
			List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());

			// Find and remove order detail with matching roomId
			for (OrderDetail detail : orderDetails) {
				if (detail.getRoomId().equals(roomId)) {
					orderDetailRepository.delete(detail);

					// If no more details, delete the order
					List<OrderDetail> remainingDetails = orderDetailRepository.findByOrderId(order.getOrderId());
					if (remainingDetails.isEmpty()) {
						orderRepository.delete(order);
					}

					System.out.println("Removed cart item successfully");
					return;
				}
			}
		}
	}

	@Override
	@Transactional
	public void clearCart(Integer userId) {
		System.out.println("=== Clearing cart for userId: " + userId + " ===");

		// Find all cart orders
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");

		for (Order order : cartOrders) {
			// Delete all order details first
			List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
			orderDetailRepository.deleteAll(orderDetails);

			// Delete the order
			orderRepository.delete(order);
		}

		System.out.println("Cart cleared successfully");
	}

	@Override
	public int getCartItemCount(Integer userId) {
		List<CartItemDto> items = getCartItems(userId);
		return items.size();
	}

	@Override
	@Transactional
	public List<Integer> checkout(Integer userId) {
		System.out.println("=== Checkout for userId: " + userId + " ===");

		// Get all cart orders
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");

		if (cartOrders.isEmpty()) {
			throw new RuntimeException("Giỏ hàng trống");
		}

		List<Integer> orderIds = new ArrayList<>();

		// Change status from CART to PENDING
		for (Order order : cartOrders) {
			order.setStatus("PENDING");
			orderRepository.save(order);
			orderIds.add(order.getOrderId());

			// Update order details status
			List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
			for (OrderDetail detail : orderDetails) {
				detail.setStatus("PENDING");
				orderDetailRepository.save(detail);
			}
		}

		System.out.println("Checkout completed. " + orderIds.size() + " orders created");
		return orderIds;
	}

	@Override
	@Transactional
	public int fixLegacyCartStatus(Integer userId) {
		System.out.println("=== Fixing legacy cart status for userId: " + userId + " ===");
		
		// Find all PENDING orders that should be CART
		// (Orders without confirmed status that are still in cart)
		List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, "PENDING");
		
		int fixed = 0;
		for (Order order : pendingOrders) {
			// Check if order details have CART status
			List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());
			boolean isCart = details.stream().anyMatch(d -> "CART".equals(d.getStatus()));
			
			if (isCart || details.isEmpty()) {
				// This is a cart order, fix the status
				order.setStatus("CART");
				orderRepository.save(order);
				
				// Also fix order details
				for (OrderDetail detail : details) {
					if (!"CART".equals(detail.getStatus())) {
						detail.setStatus("CART");
						orderDetailRepository.save(detail);
					}
				}
				
				fixed++;
				System.out.println("Fixed order " + order.getOrderId() + " to CART status");
			}
		}
		
		System.out.println("Fixed " + fixed + " legacy cart orders");
		return fixed;
	}
}
