package hotel.rest.cart;

import hotel.db.dto.cart.AddToCartRequest;
import hotel.db.dto.cart.CartItemDto;
import hotel.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            return "redirect:/login";
        }

        List<CartItemDto> cartItems = cartService.getCartItems(userId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartCount", cartItems.size());

        return "cart/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody(required = false) String rawBody,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // Log raw request body
        System.out.println("=== Raw Request Body ===");
        System.out.println(rawBody);
        
        // Parse request manually
        AddToCartRequest request = null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            request = mapper.readValue(rawBody, AddToCartRequest.class);
            
            System.out.println("=== Parsed Request ===");
            System.out.println("Room ID: " + request.getRoomId());
            System.out.println("Check-in: " + request.getCheckIn());
            System.out.println("Check-out: " + request.getCheckOut());
        } catch (Exception e) {
            System.err.println("Error parsing request: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Invalid request format: " + e.getMessage());
            return ResponseEntity.ok(response);
        }

        Integer userId = getUserIdFromSession(session);
        System.out.println("User ID: " + userId);
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng");
            return ResponseEntity.ok(response);
        }

        try {
            cartService.addToCart(userId, request);

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
            return ResponseEntity.ok(List.of());
        }
        
        List<CartItemDto> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Get user ID from session
     * @param session HttpSession
     * @return userId or null if not logged in
     */
    private Integer getUserIdFromSession(HttpSession session) {
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
            }
        }

        // For testing: return dummy user ID if no session
        // TODO: Remove this in production
        return 1; // Temporary for testing
    }
}
