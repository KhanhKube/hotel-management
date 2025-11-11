package hotel.service.payment;

import hotel.db.dto.cart.CartItemDto;
import hotel.db.dto.payment.CreatePaymentLinkRequestBody;
import hotel.db.entity.Order;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PayOS payOS;
	private final CartService cartService;
	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;

	@Override
	public CreatePaymentLinkResponse createPaymentLink(Integer userId, CreatePaymentLinkRequestBody requestBody) throws Exception {
		// Get cart items
		List<CartItemDto> cartItems = cartService.getCartItems(userId);

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Giỏ hàng trống");
		}

		// Calculate subtotal
		BigDecimal subtotal = cartItems.stream()
				.map(CartItemDto::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Apply discount if provided
		BigDecimal totalAmount = subtotal;
		if (requestBody.getDiscountId() != null) {
			// Get discount from database
			hotel.db.entity.Discount discount = cartService.getAvailableDiscounts().stream()
					.filter(d -> d.getDiscountId().equals(requestBody.getDiscountId()))
					.findFirst()
					.orElse(null);

			if (discount != null) {
				// Calculate discount amount
				BigDecimal discountPercent = BigDecimal.valueOf(discount.getValue());
				BigDecimal discountAmount = subtotal.multiply(discountPercent)
						.divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);

				totalAmount = subtotal.subtract(discountAmount);

				// Ensure total is not negative
				if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
					totalAmount = BigDecimal.ZERO;
				}

				System.out.println("Applied discount: " + discount.getCode() + " (" + discount.getValue() + "%)");
				System.out.println("Subtotal: " + subtotal + " -> Total after discount: " + totalAmount);
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

		// Store orderCode in all CART orders for this user
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");
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
	public void updateCartOrdersAfterPayment(Integer userId) {
		System.out.println("=== Updating cart orders after payment for user: " + userId + " ===");

		// Find all CART orders for this user
		List<Order> cartOrders = orderRepository.findByUserIdAndStatus(userId, "CART");

		if (cartOrders.isEmpty()) {
			System.out.println("No cart orders found for user " + userId);
			return;
		}

		System.out.println("Found " + cartOrders.size() + " cart orders to update");

		// Update all orders to COMPLETED status (payment successful)
		for (Order order : cartOrders) {
			order.setStatus("COMPLETED");
			orderRepository.save(order);

			// Update order details to RESERVED status (room is reserved)
			List<hotel.db.entity.OrderDetail> orderDetails =
					orderDetailRepository.findByOrderId(order.getOrderId());
			for (hotel.db.entity.OrderDetail detail : orderDetails) {
				detail.setStatus("RESERVED");
				orderDetailRepository.save(detail);
			}

			System.out.println("Updated order " + order.getOrderId() + " to COMPLETED status with " + orderDetails.size() + " RESERVED rooms");
		}

		System.out.println("=== Successfully updated " + cartOrders.size() + " orders to COMPLETED ===");
	}
}
