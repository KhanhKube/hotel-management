package hotel.service.payment;

import hotel.db.dto.cart.CartItemDto;
import hotel.db.dto.payment.CreatePaymentLinkRequestBody;
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

	@Override
	public CreatePaymentLinkResponse createPaymentLink(Integer userId, CreatePaymentLinkRequestBody requestBody) throws Exception {
		// Get cart items
		List<CartItemDto> cartItems = cartService.getCartItems(userId);

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Giỏ hàng trống");
		}

		// Calculate total amount
		BigDecimal totalAmount = cartItems.stream()
				.map(CartItemDto::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

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

		// Store orderCode in session or database for later verification
		// You might want to create a Payment entity to track this

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

			// TODO: Update order status from CART to PENDING
			// After payment success, you need to:
			// 1. Get orderCode from webhook (check webhookData structure)
			// 2. Find orders with that orderCode
			// 3. Update status from CART to PENDING

			// For now, just log success
			System.out.println("Payment webhook processed successfully");

		} catch (Exception e) {
			System.err.println("Error handling payment webhook: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
