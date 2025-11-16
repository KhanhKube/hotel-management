package hotel.service.payment;

import hotel.db.dto.cart.CartItemDto;
import hotel.db.dto.cart.CartSummaryDto;
import hotel.db.dto.payment.CreatePaymentLinkRequestBody;
import hotel.db.entity.Discount;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.Room;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PayOS payOS;
	private final CartService cartService;
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final RoomRepository roomRepository;

	@Override
	public CreatePaymentLinkResponse createPaymentLink(Integer userId, CreatePaymentLinkRequestBody requestBody) throws Exception {
		// Get cart items
		List<CartItemDto> cartItems = cartService.getCartItems(userId);

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Giỏ hàng trống");
		}

		// Filter cart items by selected order IDs if provided
		if (requestBody.getSelectedOrderIds() != null && !requestBody.getSelectedOrderIds().isEmpty()) {
			cartItems = cartItems.stream()
					.filter(item -> requestBody.getSelectedOrderIds().contains(item.getOrderId()))
					.collect(Collectors.toList());
			
			if (cartItems.isEmpty()) {
				throw new RuntimeException("Không có đơn hàng nào được chọn");
			}
			
			System.out.println("Processing payment for " + cartItems.size() + " selected orders");
		}

		// Calculate subtotal
		BigDecimal subtotal = cartItems.stream()
				.map(CartItemDto::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Apply discount if provided (use discountCode, fallback to discountId for backward compatibility)
		BigDecimal totalAmount = subtotal;
		String discountCodeToUse = requestBody.getDiscountCode();
		
		// Fallback to discountId if discountCode not provided (backward compatibility)
		if (discountCodeToUse == null && requestBody.getDiscountId() != null) {
			Discount discountById = cartService.getAvailableDiscounts().stream()
					.filter(d -> d.getDiscountId().equals(requestBody.getDiscountId()))
					.findFirst()
					.orElse(null);
			if (discountById != null) {
				discountCodeToUse = discountById.getCode();
			}
		}
		
		if (discountCodeToUse != null && !discountCodeToUse.trim().isEmpty()) {
			// Use CartService to get summary with discount validation
			CartSummaryDto summary = cartService.getCartSummary(
					userId, 
					requestBody.getSelectedOrderIds(), 
					discountCodeToUse
			);
			
			if (summary.getDiscountValid() && summary.getDiscountAmount() != null) {
				totalAmount = summary.getTotalAmount();
				System.out.println("Applied discount code: " + discountCodeToUse);
				System.out.println("Subtotal: " + subtotal + " -> Total after discount: " + totalAmount);
			} else {
				System.out.println("Invalid discount code: " + discountCodeToUse + " - " + summary.getDiscountMessage());
			}
		}

		long orderCode = System.currentTimeMillis() / 1000;

		// Create description (max 25 characters for PayOS)
		String description = "Dat phong #" + orderCode;

		// Create payment link item
		PaymentLinkItem item = PaymentLinkItem.builder()
				.name("Dat phong - " + cartItems.size() + " phong")
				.quantity(1)
				.price(totalAmount.longValue())
				.build();

		CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
				.orderCode(orderCode)
				.description(description)
				.amount(totalAmount.longValue())
				.item(item)
				.returnUrl(requestBody.getReturnUrl())
				.cancelUrl(requestBody.getCancelUrl())
				.build();

		CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

		// Store orderCode in selected CART orders
		List<Order> cartOrders;
		if (requestBody.getSelectedOrderIds() != null && !requestBody.getSelectedOrderIds().isEmpty()) {
			// Only update selected orders
			cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART").stream()
					.filter(order -> requestBody.getSelectedOrderIds().contains(order.getOrderId()))
					.collect(Collectors.toList());
		} else {
			// Update all cart orders (backward compatibility)
			cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");
		}
		
		for (Order order : cartOrders) {
			order.setPaymentOrderCode(orderCode);
			orderRepository.save(order);
		}

		System.out.println("Stored payment orderCode " + orderCode + " for " + cartOrders.size() + " cart orders");

		return response;
	}

	@Override
	public WebhookData verifyWebhook(Object body) throws Exception {
		return payOS.webhooks().verify(body);
	}

	@Override
	@Transactional
	public void handlePaymentWebhook(WebhookData webhookData) {
		try {
			// Log webhook data for debugging
			System.out.println("=== Payment Webhook Received ===");
			System.out.println("Webhook data: " + webhookData);

			// Get payment info
			String code = webhookData.getCode();
			String desc = webhookData.getDesc();

			System.out.println("Code: " + code);
			System.out.println("Description: " + desc);

			// Check if payment is successful (code "00" means success)
			if (!"00".equals(code)) {
				System.out.println("Payment failed or pending: " + desc);
				return;
			}

			System.out.println("Payment successful!");

			// Note: Order status is updated when user returns to success page
			// This webhook is just for logging/verification
			System.out.println("Payment webhook received and verified successfully");

		} catch (Exception e) {
			System.err.println("Error handling payment webhook: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	@Transactional
	public void updateCartOrdersAfterPayment(Integer userId, Long paymentOrderCode) {
		System.out.println("=== Updating cart orders after payment for user: " + userId + ", orderCode: " + paymentOrderCode + " ===");

		// Find all CART orders for this user
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");

		if (cartOrders.isEmpty()) {
			System.out.println("No cart orders found for user " + userId);
			return;
		}

		// Filter orders by paymentOrderCode if provided
		if (paymentOrderCode != null) {
			cartOrders = cartOrders.stream()
					.filter(order -> paymentOrderCode.equals(order.getPaymentOrderCode()))
					.collect(java.util.stream.Collectors.toList());
			
			if (cartOrders.isEmpty()) {
				System.out.println("No cart orders found with paymentOrderCode: " + paymentOrderCode);
				return;
			}
		}

		System.out.println("Found " + cartOrders.size() + " cart orders to update");

		// Update selected orders to COMPLETED status (payment successful)
		for (Order order : cartOrders) {
			order.setStatus("COMPLETED");

			// Get order details and set amount for each
			List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());

			for (OrderDetail detail : orderDetails) {
				// Get room price
				Room room = roomRepository.findById(detail.getRoomId()).orElse(null);
				if (room != null) {
					// Calculate number of nights
					long nights = ChronoUnit.DAYS.between(
							detail.getCheckIn().toLocalDate(),
							detail.getCheckOut().toLocalDate()
					);

					// Calculate and set amount for this order detail
					BigDecimal roomTotal = room.getPrice().multiply(BigDecimal.valueOf(nights));
					detail.setAmount(roomTotal);
				}

				// Update order detail status to RESERVED
				detail.setStatus("RESERVED");
				orderDetailRepository.save(detail);
			}

			// totalAmount đã được tính và lưu khi thêm vào giỏ hàng, chỉ cần save order
			orderRepository.save(order);

			System.out.println("Updated order " + order.getOrderId() + " to COMPLETED status with total amount: " + order.getTotalAmount() + " VND and " + orderDetails.size() + " RESERVED rooms");
		}

		System.out.println("=== Successfully updated " + cartOrders.size() + " orders to COMPLETED ===");
	}
}
