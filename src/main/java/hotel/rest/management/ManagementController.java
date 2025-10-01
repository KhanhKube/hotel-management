package hotel.rest.management;

import hotel.service.view.ViewService;
import hotel.service.size.SizeService;
import hotel.service.floor.FloorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class ManagementController {

    private final ViewService viewService;
    private final SizeService sizeService;
    private final FloorService floorService;

    @GetMapping("/location-room")
    public String locationRoomManagement(Model model) {
        // Thống kê tổng quan
        long totalViews = viewService.getAllViews().size();
        long totalSizes = sizeService.getAllSizes().size();
        long totalFloors = floorService.getAllFloors().size();
        
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalSizes", totalSizes);
        model.addAttribute("totalFloors", totalFloors);
        
        return "management/location-room-management";
    }
}
