package hotel.rest.management;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;
import hotel.service.view.ViewService;
import hotel.service.size.SizeService;
import hotel.service.floor.FloorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class ManagementController {

    private final ViewService viewService;
    private final SizeService sizeService;
    private final FloorService floorService;

    @GetMapping("/location-room")
    public String locationRoomManagement(Model model) {
        // Get counts for statistics
        int viewCount = viewService.getAllViews().size();
        int sizeCount = sizeService.getAllActiveSizes().size();
        int floorCount = floorService.getAllActiveFloors().size();
        int roomCount = 0; // Placeholder for room count

        model.addAttribute("viewCount", viewCount);
        model.addAttribute("sizeCount", sizeCount);
        model.addAttribute("floorCount", floorCount);
        model.addAttribute("roomCount", roomCount);

        return "management/location-room-management";
    }

    // SIZE MANAGEMENT
    @GetMapping("/size")
    public String sizeManagement(Model model) {
        model.addAttribute("sizes", sizeService.getAllActiveSizes());
        return "management/size-management";
    }

    @GetMapping("/size/new")
    public String newSizeForm(Model model) {
        model.addAttribute("sizeRequest", new SizeRequestDto());
        return "management/size-form";
    }

    @PostMapping("/size/save")
    public String saveSize(@ModelAttribute("sizeRequest") SizeRequestDto sizeRequest, RedirectAttributes redirectAttributes) {
        try {
            sizeService.createSize(sizeRequest);
            redirectAttributes.addFlashAttribute("success", "add");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add");
        }
        return "redirect:/hotel-management/size";
    }

    @GetMapping("/size/edit/{id}")
    public String editSize(@PathVariable Integer id, Model model) {
        SizeResponseDto sizeResponse = sizeService.getSizeById(id);
        SizeRequestDto sizeRequest = new SizeRequestDto();
        sizeRequest.setSize(sizeResponse.getSize());
        model.addAttribute("sizeRequest", sizeRequest);
        model.addAttribute("sizeId", id);
        return "management/size-form";
    }

    @PostMapping("/size/update/{id}")
    public String updateSize(@PathVariable Integer id, @ModelAttribute("sizeRequest") SizeRequestDto sizeRequest, RedirectAttributes redirectAttributes) {
        try {
            sizeService.updateSize(id, sizeRequest);
            redirectAttributes.addFlashAttribute("success", "edit");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "edit");
        }
        return "redirect:/hotel-management/size";
    }

    @PostMapping("/size/delete/{id}")
    public String deleteSize(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sizeService.deleteSize(id);
            redirectAttributes.addFlashAttribute("success", "delete");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete");
        }
        return "redirect:/hotel-management/size";
    }

    // FLOOR MANAGEMENT
    @GetMapping("/floor")
    public String floorManagement(Model model) {
        model.addAttribute("floors", floorService.getAllActiveFloors());
        return "management/floor-management";
    }

    @GetMapping("/floor/new")
    public String newFloorForm(Model model) {
        model.addAttribute("floorRequest", new FloorRequestDto());
        return "management/floor-form";
    }

    @PostMapping("/floor/save")
    public String saveFloor(@ModelAttribute("floorRequest") FloorRequestDto floorRequest, RedirectAttributes redirectAttributes) {
        try {
            floorService.createFloor(floorRequest);
            redirectAttributes.addFlashAttribute("success", "add");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add");
        }
        return "redirect:/hotel-management/floor";
    }

    @GetMapping("/floor/edit/{id}")
    public String editFloor(@PathVariable Integer id, Model model) {
        FloorResponseDto floorResponse = floorService.getFloorById(id);
        FloorRequestDto floorRequest = new FloorRequestDto();
        floorRequest.setFloorNumber(floorResponse.getFloorNumber());
        model.addAttribute("floorRequest", floorRequest);
        model.addAttribute("floorId", id);
        return "management/floor-form";
    }

    @PostMapping("/floor/update/{id}")
    public String updateFloor(@PathVariable Integer id, @ModelAttribute("floorRequest") FloorRequestDto floorRequest, RedirectAttributes redirectAttributes) {
        try {
            floorService.updateFloor(id, floorRequest);
            redirectAttributes.addFlashAttribute("success", "edit");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "edit");
        }
        return "redirect:/hotel-management/floor";
    }

    @PostMapping("/floor/delete/{id}")
    public String deleteFloor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            floorService.deleteFloor(id);
            redirectAttributes.addFlashAttribute("success", "delete");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete");
        }
        return "redirect:/hotel-management/floor";
    }

    @GetMapping("/rooms")
    public String roomManagement(Model model) {
        model.addAttribute("message", "Room management page");
        return "management/room-management";
    }
}
