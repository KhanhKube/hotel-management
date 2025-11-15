package hotel.service.cart;

import hotel.db.dto.cart.AddToCartRequest;
import hotel.db.dto.cart.CartItemDto;
import hotel.db.dto.cart.CartSummaryDto;
import hotel.db.entity.*;
import hotel.db.repository.discount.DiscountRepository;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final RoomRepository roomRepository;
	private final RoomImageRepository roomImageRepository;
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final DiscountRepository discountRepository;

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

		// Normalize dates to start of day (remove time component)
		LocalDateTime checkInDate = request.getCheckIn().toLocalDate().atStartOfDay();
		LocalDateTime checkOutDate = request.getCheckOut().toLocalDate().atStartOfDay();

		Room room = roomRepository.findById(request.getRoomId())
				.orElseThrow(() -> new RuntimeException("Room not found with ID: " + request.getRoomId()));

		// Calculate number of days
		long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
		if (days <= 0) {
			throw new RuntimeException("Check-out date must be after check-in date");
		}

		// Check if room is already in user's cart with overlapping dates
		List<Order> userCartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");
		for (Order cartOrder : userCartOrders) {
			List<OrderDetail> cartDetails = orderDetailRepository.findByOrderId(cartOrder.getOrderId());
			for (OrderDetail detail : cartDetails) {
				if (detail.getRoomId().equals(request.getRoomId())) {
					// Check date overlap
					LocalDateTime existingCheckIn = detail.getCheckIn().toLocalDate().atStartOfDay();
					LocalDateTime existingCheckOut = detail.getCheckOut().toLocalDate().atStartOfDay();

					if (checkInDate.isBefore(existingCheckOut) && checkOutDate.isAfter(existingCheckIn)) {
						throw new RuntimeException("Phòng này đã có trong giỏ hàng với ngày trùng lặp");
					}
				}
			}
		}

		// Check if room is already reserved by others for these dates
		List<OrderDetail> reservedDetails = orderDetailRepository.findByRoomIdAndStatus(request.getRoomId(), "RESERVED");
		for (OrderDetail detail : reservedDetails) {
			LocalDateTime existingCheckIn = detail.getCheckIn().toLocalDate().atStartOfDay();
			LocalDateTime existingCheckOut = detail.getCheckOut().toLocalDate().atStartOfDay();

			if (checkInDate.isBefore(existingCheckOut) && checkOutDate.isAfter(existingCheckIn)) {
				throw new RuntimeException("Phòng này đã được đặt cho ngày bạn chọn");
			}
		}

		// Calculate total amount for this order
		BigDecimal totalAmount = room.getPrice().multiply(BigDecimal.valueOf(days));

		// Create Order with status CART
		Order order = new Order();
		order.setUserId(userId);
		order.setFloorId(room.getFloorId());
		order.setCheckIn(checkInDate);
		order.setCheckOut(checkOutDate);
		order.setStatus("CART");
		order.setTotalAmount(totalAmount); // Lưu tổng tiền ngay khi thêm vào giỏ

		Order savedOrder = orderRepository.save(order);
		System.out.println("Created cart order with ID: " + savedOrder.getOrderId() + " with totalAmount: " + totalAmount);

		// Create OrderDetail (không set amount ở đây, sẽ set khi thanh toán thành công)
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.setOrderId(savedOrder.getOrderId());
		orderDetail.setUserId(userId);
		orderDetail.setRoomId(room.getRoomId());
		orderDetail.setFloorId(room.getFloorId());
		orderDetail.setStartDate(checkInDate);
		orderDetail.setEndDate(checkOutDate);
		orderDetail.setCheckIn(checkInDate);
		orderDetail.setCheckOut(checkOutDate);
		orderDetail.setStatus("CART");
		orderDetail.setOrderDescription(request.getDescription());
		// amount sẽ được set khi thanh toán thành công

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
				cartItem.setOrderId(order.getOrderId());
				cartItem.setRoomId(room.getRoomId());
				cartItem.setRoomType(room.getRoomType());
				cartItem.setRoomNumber(room.getRoomNumber());
				cartItem.setPrice(room.getPrice());
				cartItem.setCheckIn(detail.getCheckIn());
				cartItem.setCheckOut(detail.getCheckOut());
				cartItem.setNumberOfDays((int) days);
				cartItem.setTotalPrice(totalPrice);
				cartItem.setImageRoom(imageUrl);
				cartItem.setOrderDescription(detail.getOrderDescription());

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
					// Get room to calculate amount to subtract
					Room room = roomRepository.findById(detail.getRoomId()).orElse(null);
					if (room != null) {
						long days = ChronoUnit.DAYS.between(detail.getCheckIn(), detail.getCheckOut());
						BigDecimal amountToRemove = room.getPrice().multiply(BigDecimal.valueOf(days));
						
						// Update order total amount
						BigDecimal currentTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
						order.setTotalAmount(currentTotal.subtract(amountToRemove));
					}
					
					orderDetailRepository.delete(detail);

					// If no more details, delete the order
					List<OrderDetail> remainingDetails = orderDetailRepository.findByOrderId(order.getOrderId());
					if (remainingDetails.isEmpty()) {
						orderRepository.delete(order);
					} else {
						// Save updated order with new total
						orderRepository.save(order);
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

	@Override
	public List<Discount> getAvailableDiscounts() {
		LocalDate today = LocalDate.now();

		// Lấy tất cả discounts còn hạn và còn lượt sử dụng
		return discountRepository.findAll().stream()
				.filter(discount -> !discount.getIsDeleted())
				.filter(discount -> discount.getStartDate().isBefore(today.plusDays(1))
						&& discount.getEndDate().isAfter(today.minusDays(1)))
				.filter(discount -> discount.getUsedCount() < discount.getUsageLimit())
				.collect(Collectors.toList());
	}

	@Override
	public List<Discount> getAvailableDiscountsForCart(Integer userId) {
		// Lấy các loại phòng trong giỏ hàng
		List<CartItemDto> cartItems = getCartItems(userId);
		if (cartItems.isEmpty()) {
			return new ArrayList<>();
		}

		// Lấy danh sách room types
		Set<String> roomTypesInCart = cartItems.stream()
				.map(CartItemDto::getRoomType)
				.collect(Collectors.toSet());

		System.out.println("Room types in cart: " + roomTypesInCart);

		// Lấy discounts khả dụng và lọc theo room type
		LocalDate today = LocalDate.now();

		return discountRepository.findAll().stream()
				.filter(discount -> !discount.getIsDeleted())
				.filter(discount -> discount.getStartDate().isBefore(today.plusDays(1))
						&& discount.getEndDate().isAfter(today.minusDays(1)))
				.filter(discount -> discount.getUsedCount() < discount.getUsageLimit())
				.filter(discount -> roomTypesInCart.contains(discount.getRoomType()))
				.limit(5) // Giới hạn 5 voucher để giao diện đẹp
				.collect(Collectors.toList());
	}

	@Override
	public CartSummaryDto getCartSummary(Integer userId, List<Integer> selectedOrderIds, String discountCode) {
		System.out.println("=== Calculating cart summary for userId: " + userId + " ===");

		// Get all cart orders
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");
		
		CartSummaryDto summary = new CartSummaryDto();
		summary.setTotalOrders(cartOrders.size());
		
		// Filter by selected order IDs if provided
		if (selectedOrderIds != null && !selectedOrderIds.isEmpty()) {
			cartOrders = cartOrders.stream()
					.filter(order -> selectedOrderIds.contains(order.getOrderId()))
					.collect(Collectors.toList());
			summary.setSelectedOrders(cartOrders.size());
			summary.setSelectedOrderIds(selectedOrderIds);
		} else {
			// If no selection, select all
			summary.setSelectedOrders(cartOrders.size());
			summary.setSelectedOrderIds(cartOrders.stream()
					.map(Order::getOrderId)
					.collect(Collectors.toList()));
		}
		
		// Calculate subtotal from Order.totalAmount (already calculated in backend)
		BigDecimal subtotal = cartOrders.stream()
				.map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		summary.setSubtotal(subtotal);
		
		// Apply discount if code provided
		BigDecimal discountAmount = BigDecimal.ZERO;
		summary.setDiscountValid(false);
		
		if (discountCode != null && !discountCode.trim().isEmpty()) {
			Discount discount = discountRepository.findByCodeAndIsDeletedFalse(discountCode.trim().toUpperCase());
			
			if (discount == null) {
				summary.setDiscountMessage("Mã giảm giá không tồn tại");
				summary.setDiscountValid(false);
			} else if (discount.getIsDeleted()) {
				summary.setDiscountMessage("Mã giảm giá đã bị xóa");
				summary.setDiscountValid(false);
			} else {
				LocalDate today = LocalDate.now();
				
				// Check if discount is within valid date range
				if (today.isBefore(discount.getStartDate())) {
					summary.setDiscountMessage("Mã giảm giá chưa có hiệu lực (bắt đầu từ " + discount.getStartDate() + ")");
					summary.setDiscountValid(false);
				} else if (today.isAfter(discount.getEndDate())) {
					summary.setDiscountMessage("Mã giảm giá đã hết hạn (hết hạn ngày " + discount.getEndDate() + ")");
					summary.setDiscountValid(false);
				} else if (discount.getUsedCount() >= discount.getUsageLimit()) {
					summary.setDiscountMessage("Mã giảm giá đã hết lượt sử dụng");
					summary.setDiscountValid(false);
				} else {
					// Valid discount - calculate discount amount
					BigDecimal discountPercent = BigDecimal.valueOf(discount.getValue());
					discountAmount = subtotal.multiply(discountPercent)
							.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
					
					summary.setDiscountMessage(" Áp dụng mã " + discount.getCode() + " - Giảm " + discount.getValue() + "%");
					summary.setDiscountValid(true);
					
					System.out.println("Applied discount: " + discount.getCode() + " (" + discount.getValue() + "%)");
				}
			}
		}
		
		summary.setDiscountAmount(discountAmount);
		
		// Calculate final total
		BigDecimal totalAmount = subtotal.subtract(discountAmount);
		if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
			totalAmount = BigDecimal.ZERO;
		}
		
		summary.setTotalAmount(totalAmount);
		
		System.out.println("Cart summary - Subtotal: " + subtotal + ", Discount: " + discountAmount + ", Total: " + totalAmount);
		
		return summary;
	}

	@Override
	@Transactional
	public void updateOrderNote(Integer userId, Integer orderId, String note) {
		System.out.println("=== Updating order note for orderId: " + orderId + ", userId: " + userId + " ===");
		
		// Find the order
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));
		
		// Verify ownership and status
		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("Unauthorized: Order does not belong to user");
		}
		
		if (!"CART".equals(order.getStatus())) {
			throw new RuntimeException("Cannot update note: Order is not in cart");
		}
		
		// Update note in OrderDetail
		List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
		if (!orderDetails.isEmpty()) {
			OrderDetail detail = orderDetails.get(0); // Mỗi order chỉ có 1 detail (1 phòng)
			detail.setOrderDescription(note);
			orderDetailRepository.save(detail);
			System.out.println("Updated order note successfully");
		}
	}
}
