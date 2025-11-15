package hotel.rest.room;

import hotel.db.dto.furnishing.FurnishingFormDto;
import hotel.db.dto.room.ListIdRoomResponse;
import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.RoomListDto;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.entity.RoomMaintenance;
import hotel.db.entity.User;
import hotel.db.enums.BedType;
import hotel.db.enums.RoomStatus;
import hotel.db.enums.RoomSystemStatus;
import hotel.db.enums.RoomType;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.service.booking.BookingService;
import hotel.service.image.ImageService;
import hotel.service.room.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hotel.db.enums.RoomStatus.AVAILABLE;
import static hotel.db.enums.RoomStatus.OCCUPIED;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/room")
public class RoomController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final hotel.service.cloudinary.CloudinaryService cloudinaryService;
    private final ImageService roomImageService;
    private final RoomImageRepository roomImageRepository;

    // Tự động load data cho dropdown trong mọi request
    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("roomTypes", RoomType.ALL);
        model.addAttribute("bedTypes", BedType.ALL);
        model.addAttribute("statuses", RoomStatus.ALL);
        model.addAttribute("systemStatus", RoomSystemStatus.ALL);
        model.addAttribute("floors", roomService.getAllFloors());
        model.addAttribute("sizes", roomService.getAllSizes());
    }

    @GetMapping
    public String view(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String systemstatus,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Double size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Model model) {

        Page<RoomListDto> roomPage = roomService.getRoomListForManagement(
                search, roomType, status, systemstatus, floor, size, minPrice, maxPrice, sortBy, page, pageSize
        );

        model.addAttribute("listRoom", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalElements", roomPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        return "management/room/room-management";
    }

    @GetMapping("/create")
    public String create(Model model) {
        Room room = new Room();
        model.addAttribute("room", room);
        // Load danh sách vật dụng cho form
        model.addAttribute("furnishings", roomService.getFurnishingsForForm(null));
        return "management/room/room-create-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("room") Room room,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "furnishingIds", required = false) List<Integer> furnishingIds,
                         @RequestParam(value = "furnishingQuantities", required = false) List<Integer> furnishingQuantities,
                         @RequestParam(value = "imagesToDelete", required = false) String imagesToDelete,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        try {
            boolean isUpdate = room.getRoomId() != null;
            
            // Xử lý status
            if (!isUpdate) {
                room.setStatus(AVAILABLE);
                room.setSystemStatus(RoomSystemStatus.WORKING);
            } else {
                Room existingRoom = roomService.getRoomById(room.getRoomId());
                room.setStatus(existingRoom.getStatus());
                room.setSystemStatus(existingRoom.getSystemStatus());
            }
            
            // Validate số phòng (chỉ khi create)
            if (!isUpdate && roomService.existsByRoomNumber(room.getRoomNumber())) {
                model.addAttribute("room", room);
                model.addAttribute("furnishings", roomService.getFurnishingsForForm(
                    room.getRoomId(), furnishingIds, furnishingQuantities
                ));
                model.addAttribute("errorMessage", "Số phòng này đã tồn tại!");
                return "management/room/room-create-form";
            }
            
            // Lưu phòng
            HashMap<String, String> saveResult = roomService.saveRoom(room);
            if (saveResult.containsKey("error")) {
                model.addAttribute("room", room);
                model.addAttribute("furnishings", roomService.getFurnishingsForForm(
                    room.getRoomId(), furnishingIds, furnishingQuantities
                ));
                model.addAttribute("errorMessage", saveResult.get("error"));
                return "management/room/room-create-form";
            }
            
            // Xóa ảnh đã đánh dấu
            if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
                String[] ids = imagesToDelete.split(",");
                for (String id : ids) {
                    try {
                        roomImageService.deleteRoomImage(Integer.parseInt(id.trim()));
                    } catch (Exception e) {
                        // Log và tiếp tục
                    }
                }
            }
            
            // Upload ảnh mới
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imageUrl = cloudinaryService.getImageUrlAfterUpload(file);
                            roomImageService.saveRoomImage(room.getRoomId(), imageUrl);
                        } catch (Exception e) {
                            // Log và tiếp tục
                        }
                    }
                }
            }
            
            // Lưu vật dụng
            if (furnishingIds != null && !furnishingIds.isEmpty()) {
                roomService.saveRoomFurnishings(room.getRoomId(), furnishingIds, furnishingQuantities);
            }
            
            String successMessage = isUpdate ? "Đã cập nhật phòng thành công!" : "Đã tạo phòng thành công!";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/hotel-management/room";
            
        } catch (Exception e) {
            model.addAttribute("room", room);
            model.addAttribute("furnishings", roomService.getFurnishingsForForm(
                room.getRoomId(), furnishingIds, furnishingQuantities
            ));
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "management/room/room-create-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editRoomtForm(@PathVariable Integer id, Model model) {
        Room room = roomService.getRoomById(id);
        if (room == null) {
            return "redirect:/hotel-management/room";
        }
        // Load ảnh hiện có
        List<RoomImage> images = roomImageService.getImagesByRoomId(id);
        
        // Load vật dụng của phòng
        model.addAttribute("furnishings", roomService.getFurnishingsForForm(id));

        model.addAttribute("room", room);
        model.addAttribute("images", images);
        return "management/room/room-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // Lấy room để check status
        Room room = roomService.getRoomById(id);

        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng!");
            return "redirect:/hotel-management/room";
        }

        // Không cho xóa nếu phòng đang hoạt động (Đang thuê hoặc Đã đặt)
        if (OCCUPIED.equals(room.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không thể xóa phòng đang hoạt động! Phòng đang ở trạng thái: " + room.getStatus());
            return "redirect:/hotel-management/room";
        }

        // Cho phép xóa nếu Trống hoặc Bảo trì
        roomService.DeleteRoom(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phòng thành công!");
        return "redirect:/hotel-management/room";
    }

    @GetMapping("/detail/{id}")
    public String detailRoom(@PathVariable Integer id, Model model) {
        Room room = roomService.getRoomById(id);
        List<RoomImage> images = roomImageService.getImagesByRoomId(id);
        List<FurnishingFormDto> furnishings = roomService.getFurnishingsForForm(id);
        
        // Lọc chỉ lấy vật dụng có số lượng > 0
        List<FurnishingFormDto> roomFurnishings = furnishings.stream()
            .filter(f -> f.getRoomQuantity() != null && f.getRoomQuantity() > 0)
            .collect(java.util.stream.Collectors.toList());
        
        // Lấy thông tin Floor và Size
        Integer floorNumber = null;
        Double sizeValue = null;
        
        if (room.getFloorId() != null) {
            floorNumber = roomService.getAllFloors().stream()
                .filter(f -> f.getFloorId().equals(room.getFloorId()))
                .map(hotel.db.entity.Floor::getFloorNumber)
                .findFirst()
                .orElse(null);
        }
        
        if (room.getSizeId() != null) {
            sizeValue = roomService.getAllSizes().stream()
                .filter(s -> s.getSizeId().equals(room.getSizeId()))
                .map(hotel.db.entity.Size::getSize)
                .findFirst()
                .orElse(null);
        }

        model.addAttribute("room", room);
        model.addAttribute("images", images);
        model.addAttribute("furnishings", roomFurnishings);
        model.addAttribute("floorNumber", floorNumber);
        model.addAttribute("sizeValue", sizeValue);
        return "management/room/room-detail";
    }

    @GetMapping("/status/{id}")
    public String statusRoom(@PathVariable Integer id, Model model) {
        // Lấy 2 lists riêng biệt cho check-in và check-out calendar
        List<String> bookedDatesCheckIn = roomService.getBookedDatesForBookingRoom(id);
        List<String> bookedDatesCheckOut = roomService.getBookedDatesForCheckOut(id);
        List<String> disableDate = roomService.getDateToDisableRoom(id);
        
        // Lấy danh sách maintenance của phòng này
        List<RoomMaintenance> maintenanceList = roomService.getMaintenanceByRoomId(id);
        
        model.addAttribute("roomId", id);
        model.addAttribute("disableDate", disableDate);
        model.addAttribute("bookedDatesCheckIn", bookedDatesCheckIn);
        model.addAttribute("bookedDatesCheckOut", bookedDatesCheckOut);
        model.addAttribute("maintenanceList", maintenanceList);
        return "management/room/room-update-status";
    }

    @PostMapping("/maintenance")
    public String createRoomMaintenance(
            @RequestParam("roomId") Integer roomId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam(value = "des", required = false) String description,
            Model model, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                model.addAttribute("errorMessage", "Vui lòng đăng nhập!");
                return "redirect:/hotel/login";
            }

            Integer createdBy = user.getUserId();
            
            // Parse ngày bắt đầu để kiểm tra
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate startDate = LocalDate.parse(checkInDate, formatter);
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            // Lưu maintenance vào DB
            roomService.saveMaintenance(roomId, checkInDate, checkOutDate, description, createdBy);

            // Cập nhật systemStatus của phòng dựa trên thời gian
            Room room = roomService.getRoomById(roomId);
            
            if (startDate.equals(today)) {
                // Ngày bắt đầu = hôm nay
                if (now.getHour() >= 14) {
                    // Đã qua 14:00 → Chuyển ngay sang "Đang bảo trì"
                    room.setSystemStatus(RoomSystemStatus.MAINTENANCE);
                } else {
                    // Chưa đến 14:00 → Set "Sắp bảo trì", đợi scheduler
                    room.setSystemStatus(RoomSystemStatus.NEARMAINTENANCE);
                }
            } else if (startDate.isAfter(today)) {
                // Ngày bắt đầu > hôm nay → Set "Sắp bảo trì"
                room.setSystemStatus(RoomSystemStatus.NEARMAINTENANCE);
            }
            
            roomService.saveRoom(room);

            return "redirect:/hotel-management/room";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "management/room/room-update-status";
        }
    }
    
    @GetMapping("/maintenance/delete/{id}")
    public String deleteMaintenance(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin maintenance trước khi xóa
            RoomMaintenance maintenance = roomService.getMaintenanceById(id);
            if (maintenance == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lịch bảo trì!");
                return "redirect:/hotel-management/room";
            }
            
            Integer roomId = maintenance.getRoomId();
            
            // Soft delete maintenance
            roomService.deleteMaintenance(id);
            
            // Cập nhật lại trạng thái phòng về "Đang trống" và "Hoạt động"
            Room room = roomService.getRoomById(roomId);
            room.setStatus(AVAILABLE);
            room.setSystemStatus(RoomSystemStatus.WORKING);
            roomService.saveRoom(room);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa lịch bảo trì thành công!");
            return "redirect:/hotel-management/room/status/" + roomId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/hotel-management/room";
        }
    }
    
    @PostMapping("/disable")
    public String disableRoom(
            @RequestParam("roomId") Integer roomId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam(value = "des", required = false) String description,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập!");
                return "redirect:/hotel/login";
            }

            Integer createdBy = user.getUserId();
            roomService.disableRoom(roomId, checkInDate, checkOutDate, description, createdBy);
            
            // Set status phòng ngay lập tức
            Room room = roomService.getRoomById(roomId);
            room.setSystemStatus(RoomSystemStatus.NEARSTOPWORKING);
            roomService.saveRoom(room);

            redirectAttributes.addFlashAttribute("successMessage", "Đã lên lịch dừng hoạt động phòng!");
            return "redirect:/hotel-management/room";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/hotel-management/room/status/" + roomId;
        }
    }

    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<ListIdRoomResponse> listRooms(@RequestBody SearchRoomRequest request) {
        ListIdRoomResponse listRoomResponse = bookingService.listRoom(request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listRoomResponse);
    }

    @GetMapping("/api/list")
    public ResponseEntity<Map<String, Object>> getRooms() {
        ListRoomResponse rooms = roomService.getAllRoomForSearch();
        Map<String, Object> response = new HashMap<>();
        response.put("data", rooms);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }


}
