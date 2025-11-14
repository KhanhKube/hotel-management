package hotel.service.revenue;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class MonthlyRevenueExportScheduler {

    private final RevenueExportService revenueExportService;

    /**
     * Chạy vào ngày 1 hàng tháng lúc 00:00 để export doanh thu tháng trước
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void exportPreviousMonthRevenue() {
        try {
            // Get previous month
            YearMonth previousMonth = YearMonth.now().minusMonths(1);
            
            System.out.println("=== Starting monthly revenue export job for: " + previousMonth + " ===");
            
            revenueExportService.exportMonthlyRevenue(previousMonth);
            
            System.out.println("=== Monthly revenue export job completed successfully ===");
        } catch (Exception e) {
            System.err.println("Error exporting monthly revenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 35 00 * * ?", zone = "Asia/Bangkok")
    public void testDailyExport() {
        try {
            YearMonth currentMonth = YearMonth.now();
            System.out.println("=== Test export for current month: " + currentMonth + " ===");
            revenueExportService.exportMonthlyRevenue(currentMonth);
        } catch (Exception e) {
            System.err.println("Error in test export: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
