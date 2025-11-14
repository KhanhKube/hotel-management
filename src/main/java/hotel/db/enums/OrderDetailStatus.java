package hotel.db.enums;

public class OrderDetailStatus {
    // 0️⃣ Trạng thái đặt phòng
    public static final String CART = "CART"; // Trong giỏ hàng
    public static final String PENDING = "PENDING"; // Chờ thanh toán
    public static final String RESERVED = "RESERVED"; // Đã thanh toán, chờ check-in
    
    // 1️⃣ Trạng thái check-in (3 bước)
    public static final String CHECKING_IN = "CHECKING_IN"; // Lễ tân đã bắt đầu check-in
    public static final String CUSTOMER_CONFIRM = "CUSTOMER_CONFIRM"; // Khách đã xác nhận
    public static final String OCCUPIED = "OCCUPIED"; // Đang ở (Staff đã xác nhận)
    
    // 2️⃣ Trạng thái check-out (5 bước)
    public static final String NEED_CHECKOUT = "NEED_CHECKOUT"; // Lễ tân khởi tạo checkout, NV cần kiểm tra
    public static final String CHECKING_OUT = "CHECKING_OUT"; // Nhân viên đang kiểm tra (tạm thời)
    public static final String CHECKED_OUT = "CHECKED_OUT"; // Nhân viên đã kiểm tra xong, chờ LT xác nhận
    public static final String NEED_CLEAN = "NEED_CLEAN"; // Cần dọn dẹp
    public static final String CLEANING = "CLEANING"; // Đang dọn dẹp
    public static final String COMPLETED = "COMPLETED"; // Hoàn thành
    
    // Trạng thái khác
    public static final String CANCELLED = "CANCELLED"; // Đã hủy
    
    // Tương thích ngược
    public static final String CHECKED_IN = "OCCUPIED"; // Alias cho OCCUPIED
}
