package hotel.rest.cart;

import hotel.db.dto.cart.AddToCartRequest;
import hotel.db.dto.cart.CartItemDto;
import hotel.service.cart.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	@GetMapping
	public String viewCart(Model model, HttpSession session) {
		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			model.addAttribute("error", "Vui lòng đăng nhập để xem giỏ hàng");
			return "redirect:/hotel/login";
		}

		List<CartItemDto> cartItems = cartService.getCartItems(userId);

		// Get available discounts for room types in cart (max 5)
		List<hotel.db.entity.Discount> availableDiscounts = cartService.getAvailableDiscountsForCart(userId);

		model.addAttribute("cartItems", cartItems);
		model.addAttribute("cartCount", cartItems.size());
		model.addAttribute("availableDiscounts", availableDiscounts);

		return "cart/cart";
	}

	@PostMapping(value = "/add")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> addToCart(
			@RequestBody AddToCartRequest requestBody,
			HttpSession session,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<>();

		// Log raw request body
		System.out.println("=== Raw Request Body ===");
		System.out.println("Request Body: " + requestBody);
		if (requestBody != null) {
			System.out.println("Room ID: " + requestBody.getRoomId());
			System.out.println("Check In: " + requestBody.getCheckIn());
			System.out.println("Check Out: " + requestBody.getCheckOut());
			System.out.println("Early Check In: " + requestBody.getEarlyCheckIn());
		}

		Integer userId = getUserIdFromSession(session);
		System.out.println("User ID: " + userId);

		if (userId == null) {

			String currentUrl = request.getHeader("Referer"); // Trang trước đó
			session.setAttribute("redirectAfterLogin", currentUrl);

			response.put("success", false);
			response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng");
			return ResponseEntity.ok(response);
		}

		try {
			cartService.addToCart(userId, requestBody);

			int cartCount = cartService.getCartItemCount(userId);

			response.put("success", true);
			response.put("message", "Đã thêm phòng vào giỏ hàng");
			response.put("cartCount", cartCount);

			System.out.println("Success! Cart count: " + cartCount);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.err.println("Error adding to cart: " + e.getMessage());
			e.printStackTrace();

			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@DeleteMapping("/remove/{roomId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> removeFromCart(
			@PathVariable Integer roomId,
			HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			response.put("success", false);
			response.put("message", "Unauthorized");
			return ResponseEntity.ok(response);
		}

		cartService.removeFromCart(userId, roomId);

		int cartCount = cartService.getCartItemCount(userId);

		response.put("success", true);
		response.put("message", "Đã xóa khỏi giỏ hàng");
		response.put("cartCount", cartCount);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/clear")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			response.put("success", false);
			return ResponseEntity.ok(response);
		}

		cartService.clearCart(userId);

		response.put("success", true);
		response.put("cartCount", 0);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/count")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getCartCount(HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			response.put("cartCount", 0);
			return ResponseEntity.ok(response);
		}

		int count = cartService.getCartItemCount(userId);

		response.put("cartCount", count);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/checkout")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> checkout(HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			response.put("success", false);
			response.put("message", "Vui lòng đăng nhập");
			return ResponseEntity.ok(response);
		}

		try {
			List<Integer> orderIds = cartService.checkout(userId);

			response.put("success", true);
			response.put("message", "Đặt phòng thành công! Đã tạo " + orderIds.size() + " đơn hàng.");
			response.put("orderIds", orderIds);
			response.put("orderCount", orderIds.size());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Lỗi: " + e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/items")
	@ResponseBody
	public ResponseEntity<List<CartItemDto>> getCartItems(HttpSession session) {
		Integer userId = getUserIdFromSession(session);
		if (userId == null) {
			System.out.println("User not logged in - returning empty cart");
			return ResponseEntity.ok(List.of());
		}

		System.out.println("=== Getting cart items for user: " + userId + " ===");
		List<CartItemDto> items = cartService.getCartItems(userId);
		System.out.println("Returning " + items.size() + " cart items");
		return ResponseEntity.ok(items);
	}

	@GetMapping("/debug")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> debugCart(HttpSession session) {
		Map<String, Object> debug = new HashMap<>();
		Integer userId = getUserIdFromSession(session);

		debug.put("userId", userId);
		debug.put("sessionId", session.getId());

		if (userId != null) {
			List<CartItemDto> items = cartService.getCartItems(userId);
			debug.put("cartItems", items);
			debug.put("itemCount", items.size());

			// Show details of each item
			List<Map<String, Object>> itemDetails = new ArrayList<>();
			for (CartItemDto item : items) {
				Map<String, Object> detail = new HashMap<>();
				detail.put("roomId", item.getRoomId());
				detail.put("roomNumber", item.getRoomNumber());
				detail.put("checkIn", item.getCheckIn());
				detail.put("checkOut", item.getCheckOut());
				itemDetails.add(detail);
			}
			debug.put("itemDetails", itemDetails);
		}

		return ResponseEntity.ok(debug);
	}

	@PostMapping("/fix-legacy-cart")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> fixLegacyCart(HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		Integer userId = getUserIdFromSession(session);

		if (userId == null) {
			response.put("success", false);
			response.put("message", "Not logged in");
			return ResponseEntity.ok(response);
		}

		// This will be implemented in service
		int fixed = cartService.fixLegacyCartStatus(userId);

		response.put("success", true);
		response.put("fixed", fixed);
		response.put("message", "Fixed " + fixed + " cart items");

		return ResponseEntity.ok(response);
	}

	/**
	 * Get user ID from session
	 *
	 * @param session HttpSession
	 * @return userId or null if not logged in
	 */
	private Integer getUserIdFromSession(HttpSession session) {
		// Check userId attribute first
		Object userIdObj = session.getAttribute("userId");
		if (userIdObj != null) {
			return (Integer) userIdObj;
		}

		// Fallback: check if user object exists in session
		Object user = session.getAttribute("user");
		if (user != null) {
			// Try to get userId from user object
			try {
				return (Integer) user.getClass().getMethod("getUserId").invoke(user);
			} catch (Exception e) {
				// If failed, return null
				System.err.println("Failed to get userId from user object: " + e.getMessage());
			}
		}

		// Return null if not logged in - require authentication
		return null;
	}
}
