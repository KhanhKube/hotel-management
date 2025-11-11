package hotel.rest.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import hotel.db.dto.payment.ApiResponse;
import hotel.db.dto.payment.CreatePaymentLinkRequestBody;
import hotel.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * API: Create payment link for cart checkout
	 */
	@PostMapping("/create")
	@ResponseBody
	public ApiResponse<CreatePaymentLinkResponse> createPaymentLink(
			@RequestBody CreatePaymentLinkRequestBody requestBody,
			HttpSession session,
			HttpServletRequest request) {

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			return ApiResponse.error("Vui lòng đăng nhập");
		}

		try {
			// Set base URL for return/cancel URLs
			String baseUrl = getBaseUrl(request);
			requestBody.setReturnUrl(baseUrl + "/payment/success");
			requestBody.setCancelUrl(baseUrl + "/payment/cancel");

			CreatePaymentLinkResponse response = paymentService.createPaymentLink(userId, requestBody);
			return ApiResponse.success(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(e.getMessage());
		}
	}

	/**
	 * Page: Payment success
	 */
	@GetMapping("/success")
	public String paymentSuccess(
			@RequestParam(required = false) String orderCode,
			@RequestParam(required = false) String status,
			HttpSession session,
			Model model) {

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			return "redirect:/hotel/login";
		}

		// Update cart orders to PENDING status when user returns from payment
		try {
			paymentService.updateCartOrdersAfterPayment(userId);
		} catch (Exception e) {
			System.err.println("Error updating orders after payment: " + e.getMessage());
		}

		model.addAttribute("orderCode", orderCode);
		model.addAttribute("status", status);

		return "payment/success";
	}

	/**
	 * Page: Payment cancelled
	 */
	@GetMapping("/cancel")
	public String paymentCancel(HttpSession session, Model model) {
		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			return "redirect:/hotel/login";
		}

		return "payment/cancel";
	}

	/**
	 * Webhook: Handle PayOS payment notification
	 */
	@PostMapping("/payos-webhook")
	@ResponseBody
	public ApiResponse<WebhookData> payosWebhookHandler(@RequestBody Object body)
			throws JsonProcessingException, IllegalArgumentException {
		try {
			WebhookData data = paymentService.verifyWebhook(body);

			// Process payment result
			paymentService.handlePaymentWebhook(data);

			System.out.println("Webhook received: " + data);
			return ApiResponse.success("Webhook delivered", data);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(e.getMessage());
		}
	}

	private Integer getUserIdFromSession(HttpSession session) {
		Object userIdObj = session.getAttribute("userId");
		if (userIdObj != null) {
			return (Integer) userIdObj;
		}

		Object user = session.getAttribute("user");
		if (user != null) {
			try {
				return (Integer) user.getClass().getMethod("getUserId").invoke(user);
			} catch (Exception e) {
				System.err.println("Failed to get userId from user object: " + e.getMessage());
			}
		}

		return null;
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		String url = scheme + "://" + serverName;
		if ((scheme.equals("http") && serverPort != 80)
				|| (scheme.equals("https") && serverPort != 443)) {
			url += ":" + serverPort;
		}
		url += contextPath;
		return url;
	}
}
