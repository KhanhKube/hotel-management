package hotel.rest.revenue;

import hotel.service.revenue.RevenueExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/revenue")
public class RevenueController {

    private final RevenueExportService revenueExportService;

    /**
     * Export revenue for a specific month
     * Example: /hotel-management/revenue/export?month=2024-11
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportRevenue(
            @RequestParam(required = false) String month) {
        try {
            // Parse month or use previous month as default
            YearMonth targetMonth;
            if (month != null && !month.isEmpty()) {
                targetMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
            } else {
                targetMonth = YearMonth.now().minusMonths(1);
            }

            // Generate report
            String filePath = revenueExportService.generateMonthlyRevenueReport(targetMonth);
            
            // Return file as download
            File file = new File(filePath);
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export current month revenue
     */
    @GetMapping("/export/current")
    public ResponseEntity<Resource> exportCurrentMonth() {
        try {
            YearMonth currentMonth = YearMonth.now();
            String filePath = revenueExportService.generateMonthlyRevenueReport(currentMonth);
            
            File file = new File(filePath);
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export previous month revenue
     */
    @GetMapping("/export/previous")
    public ResponseEntity<Resource> exportPreviousMonth() {
        try {
            YearMonth previousMonth = YearMonth.now().minusMonths(1);
            String filePath = revenueExportService.generateMonthlyRevenueReport(previousMonth);
            
            File file = new File(filePath);
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
