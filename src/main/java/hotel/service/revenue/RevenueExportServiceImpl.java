package hotel.service.revenue;

import hotel.db.dto.revenue.MonthlyRevenueDto;
import hotel.db.entity.Order;
import hotel.db.entity.OrderDetail;
import hotel.db.entity.User;
import hotel.db.repository.order.OrderRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueExportServiceImpl implements RevenueExportService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PayOS payOS;

    @Override
    public void exportMonthlyRevenue(YearMonth month) throws IOException {
        String reportPath = generateMonthlyRevenueReport(month);
        System.out.println("Monthly revenue report generated: " + reportPath);
    }

    @Override
    public String generateMonthlyRevenueReport(YearMonth month) throws IOException {
        System.out.println("=== Generating monthly revenue report for: " + month + " ===");

        // Get start and end of month
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

        // Find all COMPLETED orders in this month
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()))
                .filter(order -> order.getUpdatedAt() != null)
                .filter(order -> !order.getUpdatedAt().isBefore(startOfMonth) 
                        && !order.getUpdatedAt().isAfter(endOfMonth))
                .collect(Collectors.toList());

        System.out.println("Found " + completedOrders.size() + " completed orders in " + month);

        // Prepare revenue data
        List<MonthlyRevenueDto> revenueList = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Order order : completedOrders) {
            // Get user info
            User user = userRepository.findById(order.getUserId()).orElse(null);
            String userName = user.getFirstName() + user.getLastName();
            String customerName = user != null ? userName: "N/A";

            // Get order details
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
            
            // Get room numbers
            String roomNumbers = orderDetails.stream()
                    .map(detail -> roomRepository.findById(detail.getRoomId())
                            .map(room -> room.getRoomNumber())
                            .orElse("N/A"))
                    .collect(Collectors.joining(", "));

            // Get total amount from order
            BigDecimal orderAmount = order.getTotalAmount();
            
            System.out.println("=== Processing Order " + order.getOrderId() + " ===");
            System.out.println("Order.totalAmount from DB: " + orderAmount);
            
            // If totalAmount is null or zero, calculate from order details
            if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) == 0) {
                orderAmount = BigDecimal.ZERO;
                System.out.println("Calculating from order details:");
                for (OrderDetail detail : orderDetails) {
                    BigDecimal detailAmount = detail.getAmount();
                    System.out.println("  - OrderDetail " + detail.getOrderDetailId() + " amount: " + detailAmount);
                    
                    // If detail amount is null, calculate from room price and nights
                    if (detailAmount == null || detailAmount.compareTo(BigDecimal.ZERO) == 0) {
                        hotel.db.entity.Room room = roomRepository.findById(detail.getRoomId()).orElse(null);
                        if (room != null && detail.getCheckIn() != null && detail.getCheckOut() != null) {
                            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                                    detail.getCheckIn().toLocalDate(),
                                    detail.getCheckOut().toLocalDate()
                            );
                            detailAmount = room.getPrice().multiply(BigDecimal.valueOf(nights));
                            System.out.println("    Calculated: " + room.getPrice() + " x " + nights + " nights = " + detailAmount);
                        }
                    }
                    
                    if (detailAmount != null) {
                        orderAmount = orderAmount.add(detailAmount);
                    }
                }
                System.out.println("Total calculated amount: " + orderAmount);
            }

            MonthlyRevenueDto dto = new MonthlyRevenueDto();
            dto.setOrderId(order.getOrderId());
            dto.setPaymentOrderCode(order.getPaymentOrderCode());
            dto.setTotalAmount(orderAmount);
            dto.setCompletedAt(order.getUpdatedAt());
            dto.setCustomerName(customerName);
            dto.setRoomNumbers(roomNumbers);
            dto.setNumberOfRooms(orderDetails.size());

            revenueList.add(dto);
            totalRevenue = totalRevenue.add(orderAmount);
            
            System.out.println("Final order amount: " + orderAmount + ", Running total: " + totalRevenue);
            System.out.println("===================");
        }

        // Create export directory if not exists
        File exportDir = new File("exports/revenue");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // Generate CSV file with timestamp to avoid file lock issues
        String baseFileName = String.format("revenue_%s", month.toString());
        String fileName = baseFileName + ".csv";
        String filePath = "exports/revenue/" + fileName;
        
        // Check if file is locked, add timestamp if needed
        File file = new File(filePath);
        int attempt = 0;
        while (file.exists() && !canWrite(file) && attempt < 5) {
            String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("_HHmmss"));
            fileName = baseFileName + timestamp + ".csv";
            filePath = "exports/revenue/" + fileName;
            file = new File(filePath);
            attempt++;
            System.out.println("File is locked, trying with new name: " + fileName);
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
            // Write BOM for UTF-8 (helps Excel recognize Vietnamese characters)
            writer.write('\ufeff');
            
            // Write title row
            writer.write(String.format("BAO CAO DOANH THU THANG %s", month.toString()));
            writer.newLine();
            writer.newLine();

            // Write header
            writer.write("STT,Khach Hang,Phong,So Phong,Thanh Toan (VND),Ngay Hoan Thanh");
            writer.newLine();

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###");
            int index = 1;
            for (MonthlyRevenueDto dto : revenueList) {
                String customerName = escapeCSV(dto.getCustomerName());
                String roomNumbers = escapeCSV(dto.getRoomNumbers());
                // Wrap amount in quotes to prevent Excel from splitting on comma
                String amount = "\"" + decimalFormat.format(dto.getTotalAmount().longValue()) + "\"";
                String completedAt = dto.getCompletedAt().format(formatter);
                
                writer.write(String.format("%d,%s,%s,%d,%s,%s",
                        index++,
                        customerName,
                        roomNumbers,
                        dto.getNumberOfRooms(),
                        amount,
                        completedAt
                ));
                writer.newLine();
            }

            // Write summary
            writer.newLine();
            writer.write("TONG KET");
            writer.newLine();
            writer.write(String.format("Tong so don hang,%d", revenueList.size()));
            writer.newLine();
            writer.write(String.format("Tong doanh thu,\"%s\"", decimalFormat.format(totalRevenue.longValue())));
            writer.newLine();
            writer.write(String.format("Thang bao cao,%s", month.toString()));
        }

        System.out.println("=== Revenue report exported to: " + filePath + " ===");
        System.out.println("Total orders: " + revenueList.size());
        System.out.println("Total revenue: " + totalRevenue + " VND");

        return filePath;
    }

    /**
     * Escape CSV special characters
     * If text contains comma, quote, or newline, wrap it in quotes
     */
    private String escapeCSV(String text) {
        if (text == null) return "";
        
        // If text contains comma, quote, or newline, wrap in quotes and escape quotes
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        
        return text;
    }

    /**
     * Check if file can be written (not locked by another process)
     */
    private boolean canWrite(File file) {
        if (!file.exists()) {
            return true;
        }
        
        // Try to open file for writing
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw")) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
