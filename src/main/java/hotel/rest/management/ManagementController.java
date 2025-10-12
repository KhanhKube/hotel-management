package hotel.rest.management;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;
import hotel.db.dto.room.RoomRequestDto;
import hotel.db.entity.Room;
import hotel.service.view.ViewService;
import hotel.service.size.SizeService;
import hotel.service.floor.FloorService;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class ManagementController {

    private final ViewService viewService;
    private final SizeService sizeService;
    private final FloorService floorService;
    private final RoomService roomService;

    @GetMapping("/location-room")
    public String locationRoomManagement(Model model) {
        // Get counts for statistics
        int viewCount = viewService.getAllViews().size();
        int sizeCount = sizeService.getAllActiveSizes().size();
        int floorCount = floorService.getAllActiveFloors().size();
        int roomCount = roomService.getAllRooms().size();

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

    // ROOM MANAGEMENT
    @GetMapping("/rooms")
    public String roomManagement(Model model) {
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "management/room-management";
    }

    @GetMapping("/rooms/new")
    public String newRoomForm(Model model) {
        RoomRequestDto roomRequest = new RoomRequestDto();
        // Set default values
        roomRequest.setRoomNumber("");
        roomRequest.setRoomType("Deluxe");
        roomRequest.setBedType("King");
        roomRequest.setFloorId(1L);
        roomRequest.setSizeId(2L);
        roomRequest.setStatus("AVAILABLE");
        roomRequest.setPrice(new java.math.BigDecimal("1000000")); // 1 triệu VNĐ
        roomRequest.setRoomDescription("Phòng deluxe với view đẹp");
        
        model.addAttribute("roomRequest", roomRequest);
        return "management/room-form";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute("roomRequest") RoomRequestDto roomRequest, RedirectAttributes redirectAttributes) {
        try {
            Room room = new Room();
            room.setRoomNumber(roomRequest.getRoomNumber());
            room.setRoomType(roomRequest.getRoomType());
            room.setBedType(roomRequest.getBedType());
            room.setFloorId(roomRequest.getFloorId().intValue());
            room.setSizeId(roomRequest.getSizeId().intValue());
            room.setRoomDescription(roomRequest.getRoomDescription());
            room.setPrice(roomRequest.getPrice());
            room.setStatus(roomRequest.getStatus());
            room.setSold(roomRequest.getSold() != null ? roomRequest.getSold() : 0);
            room.setView(roomRequest.getView() != null ? roomRequest.getView() : 0);
            
            Room savedRoom = roomService.createRoom(room);
            redirectAttributes.addFlashAttribute("success", "add");
            redirectAttributes.addFlashAttribute("newRoomId", savedRoom.getRoomId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotel-management/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Integer id, Model model) {
        try {
            Room room = roomService.getRoomById(id);
            RoomRequestDto roomRequest = new RoomRequestDto();
            roomRequest.setRoomNumber(room.getRoomNumber());
            roomRequest.setRoomType(room.getRoomType());
            roomRequest.setBedType(room.getBedType());
            roomRequest.setFloorId(room.getFloorId().longValue());
            roomRequest.setSizeId(room.getSizeId().longValue());
            roomRequest.setRoomDescription(room.getRoomDescription());
            roomRequest.setPrice(room.getPrice());
            roomRequest.setStatus(room.getStatus());
            roomRequest.setSold(room.getSold());
            roomRequest.setView(room.getView());
            
            model.addAttribute("roomRequest", roomRequest);
            model.addAttribute("roomId", id);
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy phòng với ID: " + id);
        }
        return "management/room-form";
    }

    @PostMapping("/rooms/update/{id}")
    public String updateRoom(@PathVariable Integer id, @ModelAttribute("roomRequest") RoomRequestDto roomRequest, RedirectAttributes redirectAttributes) {
        try {
            Room room = new Room();
            room.setRoomNumber(roomRequest.getRoomNumber());
            room.setRoomType(roomRequest.getRoomType());
            room.setBedType(roomRequest.getBedType());
            room.setFloorId(roomRequest.getFloorId().intValue());
            room.setSizeId(roomRequest.getSizeId().intValue());
            room.setRoomDescription(roomRequest.getRoomDescription());
            room.setPrice(roomRequest.getPrice());
            room.setStatus(roomRequest.getStatus());
            room.setSold(roomRequest.getSold() != null ? roomRequest.getSold() : 0);
            room.setView(roomRequest.getView() != null ? roomRequest.getView() : 0);
            
            Room updatedRoom = roomService.updateRoom(id, room);
            redirectAttributes.addFlashAttribute("success", "edit");
            redirectAttributes.addFlashAttribute("updatedRoomId", updatedRoom.getRoomId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "edit");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotel-management/rooms";
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            roomService.hardDeleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "delete");
            redirectAttributes.addFlashAttribute("deletedRoomId", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotel-management/rooms";
    }
}
