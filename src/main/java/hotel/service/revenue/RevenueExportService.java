package hotel.service.revenue;

import java.io.IOException;
import java.time.YearMonth;

public interface RevenueExportService {
    void exportMonthlyRevenue(YearMonth month) throws IOException;
    String generateMonthlyRevenueReport(YearMonth month) throws IOException;
}
