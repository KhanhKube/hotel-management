package hotel.service.cart;

import hotel.db.dto.cart.AddToCartRequest;
import hotel.db.dto.cart.CartItemDto;

import java.util.List;

public interface CartService {
    void addToCart(Integer userId, AddToCartRequest request);
    List<CartItemDto> getCartItems(Integer userId);
    void removeFromCart(Integer userId, Integer roomId);
    void clearCart(Integer userId);
    int getCartItemCount(Integer userId);
    List<Integer> checkout(Integer userId);
}
