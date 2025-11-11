package hotel.service.payment;

import hotel.db.dto.payment.CreatePaymentLinkRequestBody;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

public interface PaymentService {
	CreatePaymentLinkResponse createPaymentLink(Integer userId, CreatePaymentLinkRequestBody requestBody) throws Exception;

	WebhookData verifyWebhook(Object body) throws Exception;

	void handlePaymentWebhook(WebhookData webhookData);

	void updateCartOrdersAfterPayment(Integer userId);
}
