package hotel.rest.floor;

import hotel.db.entity.Floor;
import hotel.service.floor.FloorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class FloorController {

    private final FloorService floorService;

    // Hiển thị danh sách
    @GetMapping("/floor")
    public String listFloors(Model model) {
        List<Floor> floors = floorService.getAllFloors();
        model.addAttribute("floors", floors);
        return "management/floor/floor-list";
    }

    // Form thêm mới
    @GetMapping("/floor/new")
    public String createFloorForm(Model model) {
        model.addAttribute("floor", new Floor());
        return "management/floor/form";
    }

    // Lưu floor (tạo mới hoặc cập nhật)
    @PostMapping("/floor/new")
    public String saveFloor(@ModelAttribute("floor") Floor floor) {
        try {
            floorService.saveFloor(floor);
            return "redirect:/hotel-management/floor?message=Floor saved successfully";
        } catch (Exception e) {
            return "redirect:/hotel-management/floor?error=" + e.getMessage();
        }
    }

    // Form sửa
    @GetMapping("/floor/edit/{id}")
    public String editFloor(@PathVariable Integer id, Model model) {
        try {
            model.addAttribute("floor", floorService.getFloorById(id));
            return "management/floor/form";
        } catch (Exception e) {
            return "redirect:/hotel-management/floor?error=" + e.getMessage();
        }
    }

    // Xóa
    @GetMapping("/floor/delete/{id}")
    public String deleteFloor(@PathVariable Integer id) {
        try {
            floorService.deleteFloor(id);
            return "redirect:/hotel-management/floor?message=Floor deleted successfully";
        } catch (Exception e) {
            return "redirect:/hotel-management/floor?error=" + e.getMessage();
        }
    }
}
