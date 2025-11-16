package hotel.rest.management;

import hotel.db.dto.checking.OrderMaintenanceResponse;
import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;
import hotel.db.dto.room.RoomRequestDto;
import hotel.db.entity.*;
import hotel.service.order.OrderService;
import hotel.service.view.ViewService;
import hotel.service.size.SizeService;
import hotel.service.floor.FloorService;
import hotel.service.room.RoomService;
import hotel.util.MessageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class ManagementController {

    private final ViewService viewService;
    private final FloorService floorService;
    private final RoomService roomService;
    private final SizeService sizeService;
    private final OrderService orderService;

    @GetMapping("/location-room")
    public String locationRoomManagement(
            @RequestParam(value = "type", defaultValue = "floor") String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";
        // Get counts for statistics
        int viewCount = viewService.getAllViews().size();
        int sizeCount = sizeService.getAllSizes().size();
        int floorCount = floorService.getAllActiveFloors().size();

        model.addAttribute("viewCount", viewCount);
        model.addAttribute("sizeCount", sizeCount);
        model.addAttribute("floorCount", floorCount);

        // Load data based on type with pagination
        model.addAttribute("currentType", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        List<?> allItems = new ArrayList<>();
        int totalItems = 0;

        switch (type) {
            case "view":
                List<hotel.db.entity.View> allViews = viewService.getAllViews();
                allItems = allViews;
                totalItems = allViews.size();
                break;
            case "size":
                List<Size> allSizes = sizeService.getAllSizes();
                allItems = allSizes;
                totalItems = allSizes.size();
                break;
            case "floor":
            default:
                List<FloorResponseDto> allFloors = floorService.getAllActiveFloors();
                allItems = allFloors;
                totalItems = allFloors.size();
                break;
        }

        // Calculate pagination
        int totalPages = totalItems > 0 ? (int) Math.ceil((double) totalItems / size) : 0;
        int start = page * size;
        int end = Math.min(start + size, totalItems);

        // Get paginated items
        List<?> paginatedItems = new ArrayList<>();
        if (totalItems > 0 && start < totalItems) {
            paginatedItems = allItems.subList(start, end);
        }

        // Add paginated data to model
        switch (type) {
            case "view":
                model.addAttribute("views", paginatedItems);
                break;
            case "size":
                model.addAttribute("sizes", paginatedItems);
                break;
            case "floor":
            default:
                model.addAttribute("floors", paginatedItems);
                break;
        }

        model.addAttribute("totalPages", Math.max(totalPages, 1)); // At least 1 page even if empty
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", totalPages > 0 && page < totalPages - 1);
        model.addAttribute("isFirst", page == 0);
        model.addAttribute("isLast", totalPages == 0 || page >= totalPages - 1);

        return "management/location-room-management";
    }

    // SIZE MANAGEMENT
    @GetMapping("/size")
    public String sizeManagement(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";
        model.addAttribute("sizes", sizeService.getAllSizes());
        return "management/size-management";
    }

    @GetMapping("/size/new")
    public String newSizeForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";
        model.addAttribute("sizeRequest", new SizeRequestDto());
        return "management/size-form";
    }

    @PostMapping("/size/save")
    public String saveSize(@ModelAttribute("sizeRequest") SizeRequestDto sizeRequest, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) return "redirect:/hotel";
            if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
            if (user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";
            sizeService.createSize(sizeRequest);
            redirectAttributes.addFlashAttribute("success", "add");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add");
        }
        return "redirect:/hotel-management/location-room?type=size&page=0&size=10";
    }

    @GetMapping("/size/edit/{id}")
    public String editSize(@PathVariable Integer id, Model model) {
        Size size = sizeService.getSizeById(id);
        SizeRequestDto sizeRequest = new SizeRequestDto();
        sizeRequest.setSize(size.getSize());
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
        return "redirect:/hotel-management/location-room?type=size&page=0&size=10";
    }

    @PostMapping("/size/delete/{id}")
    public String deleteSize(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sizeService.deleteSize(id);
            redirectAttributes.addFlashAttribute("success", "delete");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete");
        }
        return "redirect:/hotel-management/location-room?type=size&page=0&size=10";
    }

    // FLOOR MANAGEMENT (redirect to location-room)
    @GetMapping("/floor/new")
    public String newFloorForm(Model model) {
        model.addAttribute("floorRequest", new FloorRequestDto());
        return "management/floor-form";
    }

    @PostMapping("/floor/save")
    public String saveFloor(@ModelAttribute("floorRequest") FloorRequestDto floorRequest,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            floorService.createFloor(floorRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tầng thành công!");
            return "redirect:/hotel-management/location-room?type=floor&page=0&size=10";
        } catch (Exception e) {
            // Nếu có lỗi, hiển thị lại form với error message
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("floorRequest", floorRequest);
            return "management/floor-form";
        }
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
    public String updateFloor(@PathVariable Integer id,
                              @ModelAttribute("floorRequest") FloorRequestDto floorRequest,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            floorService.updateFloor(id, floorRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tầng thành công!");
            return "redirect:/hotel-management/location-room?type=floor&page=0&size=10";
        } catch (Exception e) {
            // Nếu có lỗi, hiển thị lại form với error message
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("floorRequest", floorRequest);
            model.addAttribute("floorId", id);
            return "management/floor-form";
        }
    }

    @PostMapping("/floor/delete/{id}")
    public String deleteFloor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            floorService.deleteFloor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tầng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotel-management/location-room?type=floor&page=0&size=10";
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
            room.setMaxSizePeople(roomRequest.getMaxSizePeople());

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
            roomRequest.setMaxSizePeople(room.getMaxSizePeople());

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
            room.setMaxSizePeople(roomRequest.getMaxSizePeople());

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
    public String deleteRoom(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/hotel";
            }
            if (user.getRole().equals(CUSTOMER)) {
                return "redirect:/hotel";
            }
            if (user.getRole().equals(STAFF) || user.getRole().equals(RECEPTIONIST)) {
                return "redirect:/hotel/dashboard";
            }
            roomService.hardDeleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "delete");
            redirectAttributes.addFlashAttribute("deletedRoomId", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotel-management/rooms";
    }

    // API endpoint to check if room number exists
    @GetMapping("/api/rooms/check-exists")
    @ResponseBody
    public Map<String, Object> checkRoomNumberExists(@RequestParam String roomNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = roomService.existsByRoomNumber(roomNumber);
            response.put("exists", exists);
            response.put("message", exists ? "Phòng " + roomNumber + " đã tồn tại" : "Số phòng " + roomNumber + " có thể sử dụng");
        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", "Lỗi kiểm tra số phòng");
            response.put("error", e.getMessage());
        }
        return response;
    }

    // View room detail
    @GetMapping("/rooms/detail/{id}")
    public String viewRoomDetail(@PathVariable Integer id, Model model) {
        try {
            Room room = roomService.getRoomById(id);
            model.addAttribute("room", room);

            // Get additional room information if needed
            // You can add more services here to get room images, views, etc.

            return "management/room/room-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin phòng: " + e.getMessage());
            return "redirect:/hotel-management/rooms";
        }
    }

    @GetMapping("/room-maintenance")
    public String view(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String sortBy,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int pageSize,
                       HttpSession session,
                       Model model, RedirectAttributes redirectAttrs) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }

        Page<Room> rooms = roomService.getAllRoomsMaintenance(search, sortBy, page, pageSize);
        model.addAttribute("listRoom", rooms.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rooms.getTotalPages());
        model.addAttribute("totalElements", rooms.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        return "management/room-maintenance/room-list";

    }

    @GetMapping("/room-maintenance/{id}")
    public String viewRoomMaintenance(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session,
            Model model, RedirectAttributes redirectAttrs) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";

        // Lấy phòng theo ID
        Room room = roomService.getRoomById(id);
        if (room == null || !"đang bảo trì".equalsIgnoreCase(room.getSystemStatus())) {
            redirectAttrs.addFlashAttribute("error", "Phòng không tồn tại hoặc không phải phòng bảo trì");
            return "redirect:/hotel-management/room-maintenance";
        }
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<OrderMaintenanceResponse> orderDetails = orderService.findFutureOrdersByRoomId(id, today, pageable);
        if(orderDetails == null){
            redirectAttrs.addFlashAttribute("message", "Không có phòng nào đang bảo trì!");
            return "redirect:/hotel-management/room-maintenance";
        }
        // Add model
        model.addAttribute("room", room);
        model.addAttribute("orderDetails", orderDetails.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderDetails.getTotalPages());
        model.addAttribute("totalElements", orderDetails.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        return "management/room-maintenance/room-maintenance-detail";

    }

    @GetMapping("/room-maintenance/submit/{orderDetailId}")
    public String viewRoomMaintenance(@PathVariable Integer orderDetailId,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttrs){
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/hotel";
        if (user.getRole().equals(CUSTOMER)) return "redirect:/hotel";
        if (!user.getRole().equals(RECEPTIONIST)) return "redirect:/hotel/dashboard";
        OrderDetail orderDetail = orderService.getOrderDetail(orderDetailId);
        MessageResponse response = orderService.cancelOrderDetail(orderDetailId);
        if(!response.isSuccess()){
            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel-management/room-maintenance/" + orderDetail.getRoomId();
        }

       return "redirect:/hotel-management/room-maintenance/"+ orderDetail.getRoomId();
    }


}
