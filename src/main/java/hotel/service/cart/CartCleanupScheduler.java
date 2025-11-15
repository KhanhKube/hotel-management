package hotel.service.cart;

import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartCleanupScheduler {

	private final OrderRepository orderRepository;
	private final OrderDetailRepository orderDetailRepository;

	/**
	 * Chạy mỗi phút để xóa cart items đã quá 15 phút
	 * Cron: giây phút giờ ngày tháng thứ
	 */
	@Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
	@Transactional
	public void cleanupExpiredCartItems() {
		try {
			// Tính thời gian 15 phút trước
			LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);

			// Tìm tất cả orders có status CART và createdAt < expiryTime
			List<Order> expiredCartOrders = orderRepository.findByStatusAndCreatedAtBefore("CART", expiryTime);

			if (expiredCartOrders.isEmpty()) {
				return; // Không có gì để xóa
			}

			System.out.println("=== Cart Cleanup: Found " + expiredCartOrders.size() + " expired cart orders ===");

			for (Order order : expiredCartOrders) {
				// Xóa order details trước
				List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
				if (!orderDetails.isEmpty()) {
					orderDetailRepository.deleteAll(orderDetails);
					System.out.println("Deleted " + orderDetails.size() + " order details for order " + order.getOrderId());
				}

				// Xóa order
				orderRepository.delete(order);
				System.out.println("Deleted expired cart order " + order.getOrderId() + " (created at: " + order.getCreatedAt() + ")");
			}

			System.out.println("=== Cart Cleanup: Completed. Deleted " + expiredCartOrders.size() + " expired orders ===");

		} catch (Exception e) {
			System.err.println("Error during cart cleanup: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
