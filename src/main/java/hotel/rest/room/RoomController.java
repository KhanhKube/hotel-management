package hotel.rest.room;

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
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.service.booking.BookingService;
import hotel.service.file.RoomImageUploadService;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/room")
public class RoomController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final RoomImageUploadService fileUploadService;
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
        return "management/room/room-create-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("room") Room room,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         BindingResult result, Model model) {
        try {
            // Lưu số phòng cũ và status cũ (để đổi tên folder nếu update)
            String oldRoomNumber = null;
            String oldStatus = null;
            String oldSystemStatus = null;
            
            if (room.getRoomId() == null) {
                // Tạo mới: Set status và systemStatus mặc định
                room.setStatus("Đang trống");
                room.setSystemStatus("Hoạt động");
            } else {
                // Update: Giữ nguyên status và systemStatus cũ
                Room existingRoom = roomService.getRoomById(room.getRoomId());
                oldRoomNumber = existingRoom.getRoomNumber();
                oldStatus = existingRoom.getStatus();
                oldSystemStatus = existingRoom.getSystemStatus();
                
                // Set lại status và systemStatus từ DB (không lấy từ form)
                room.setStatus(oldStatus);
                room.setSystemStatus(oldSystemStatus);
                
                System.out.println("Old Room Number: " + oldRoomNumber);
            }

            // Validate room number
            if (room.getRoomId() == null) {
                if (roomService.checkForCreateRoomNumber(room.getRoomNumber())) {
                    model.addAttribute("room", room);
                    model.addAttribute("errorMessage", "Số phòng này đã tồn tại!");
                    return "management/room/room-create-form";
                }
            } else {
                if (!roomService.checkForEditRoomNumber(room.getRoomNumber(), Long.valueOf(room.getRoomId()))) {
                    model.addAttribute("room", room);
                    model.addAttribute("errorMessage", "Số phòng này đã tồn tại!");
                    return "management/room/room-create-form";
                }
            }

            // Save room
            HashMap<String, String> saveResult = roomService.saveRoom(room);
            if (saveResult.containsKey("error")) {
                model.addAttribute("room", room);
                model.addAttribute("errorMessage", saveResult.get("error"));
                return "management/room/room-create-form";
            }

            // Nếu đổi số phòng → đổi tên folder
            if (oldRoomNumber != null && !oldRoomNumber.equals(room.getRoomNumber())) {
                fileUploadService.renameRoomFolder(oldRoomNumber, room.getRoomNumber());

                // Update URL trong DB
                List<RoomImage> images = roomImageService.getImagesByRoomId(room.getRoomId());
                for (RoomImage image : images) {
                    String oldUrl = image.getRoomImageUrl();
                    String newUrl = oldUrl.replace("/room/" + oldRoomNumber + "/", "/room/" + room.getRoomNumber() + "/");
                    image.setRoomImageUrl(newUrl);
                    roomImageRepository.save(image);
                }
            }

            // Upload ảnh mới nếu có
            if (imageFiles != null && !imageFiles.isEmpty()) {
                System.out.println("=== UPLOADING IMAGES ===");
                // Upload với số phòng làm tên folder
                List<String> imageUrls = fileUploadService.uploadRoomImages(room.getRoomNumber(), imageFiles);
                System.out.println("Uploaded " + imageUrls.size() + " images");

                // Lưu vào DB
                for (String imageUrl : imageUrls) {
                    roomImageService.saveRoomImage(room.getRoomId(), imageUrl);
                    System.out.println("Saved image URL: " + imageUrl);
                }
            }
            return "redirect:/hotel-management/room";

        } catch (IOException e) {
            System.err.println("=== IOException: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("room", room);
            model.addAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
            return "management/room/room-create-form";
        } catch (RuntimeException e) {
            System.err.println("=== RuntimeException: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("room", room);
            model.addAttribute("errorMessage", e.getMessage());
            return "management/room/room-create-form";
        } catch (Exception e) {
            System.err.println("=== Unexpected Exception: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("room", room);
            model.addAttribute("errorMessage", "Lỗi không xác định: " + e.getMessage());
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

        model.addAttribute("room", room);
        model.addAttribute("images", images);
        return "management/room/room-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Integer id, Model model) {
        // Lấy room để check status
        Room room = roomService.getRoomById(id);

        if (room == null) {
            model.addAttribute("errorMessage", "Không tìm thấy phòng!");
            model.addAttribute("listRoom", roomService.getRoomList());
            return "management/room/room-management";
        }

        // Không cho xóa nếu phòng đang hoạt động (Đang thuê hoặc Đã đặt)
        if ("Đang thuê".equals(room.getStatus()) || "Đã đặt".equals(room.getStatus())) {
            model.addAttribute("errorMessage", "Không thể xóa phòng đang hoạt động! Phòng đang ở trạng thái: " + room.getStatus());
            model.addAttribute("listRoom", roomService.getRoomList());
            return "management/room/room-management";
        }

        // Cho phép xóa nếu Trống hoặc Bảo trì
        roomService.DeleteRoom(id);
        return "redirect:/hotel-management/room";
    }

    @GetMapping("/detail/{id}")
    public String detailRoom(@PathVariable Integer id, Model model) {
        Room room = roomService.getRoomById(id);
        List<RoomImage> images = roomImageService.getImagesByRoomId(id);

        model.addAttribute("room", room);
        model.addAttribute("images", images);
        return "management/room/room-detail";
    }

    @GetMapping("/status/{id}")
    public String statusRoom(@PathVariable Integer id, Model model) {
        // Lấy 2 lists riêng biệt cho check-in và check-out calendar
        List<String> bookedDatesCheckIn = roomService.getBookedDatesForBookingRoom(id);
        List<String> bookedDatesCheckOut = roomService.getBookedDatesForCheckOut(id);
        model.addAttribute("roomId", id);
        model.addAttribute("bookedDatesCheckIn", bookedDatesCheckIn);
        model.addAttribute("bookedDatesCheckOut", bookedDatesCheckOut);
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
            roomService.saveMaintenance(roomId, checkInDate, checkOutDate, description, createdBy);

            Room room = roomService.getRoomById(roomId);
            room.setSystemStatus("Bảo trì");
            roomService.saveRoom(room);

            return "redirect:/hotel-management/room";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "management/room/room-update-status";
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

    @PostMapping("/image/delete/{imageId}")
    public String deleteImage(@PathVariable Integer imageId, @RequestParam Integer roomId, Model model) {
        try {
            roomImageService.deleteRoomImage(imageId);
            model.addAttribute("successMessage", "Xóa ảnh thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi xóa ảnh: " + e.getMessage());
        }
        return "redirect:/hotel-management/room/edit/" + roomId;
    }
}
