package hotel.config;

import hotel.db.entity.View;
import hotel.db.repository.view.ViewRepository;
import hotel.service.common.ViewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewDataSeeder implements CommandLineRunner {

    private final ViewRepository viewRepository;
    private final ViewImageService viewImageService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize existing images first
        viewImageService.initializeDefaultImages();
        
        // Check if views already exist
        if (viewRepository.count() > 0) {
            return;
        }

        // Create sample views
        String[] viewTypes = {
            "View Biển",
            "View Núi", 
            "View Thành Phố",
            "View Vườn",
            "View Hồ Bơi",
            "View Bãi Biển",
            "View Cảng",
            "View Rừng",
            "View Sông",
            "View Thung Lũng"
        };

        for (String viewType : viewTypes) {
            View view = new View();
            view.setViewType(viewType);
            viewRepository.save(view);
        }

        System.out.println(" Đã tạo thành công các loại view mẫu!");
    }
}
