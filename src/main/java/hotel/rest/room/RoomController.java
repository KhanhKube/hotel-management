package hotel.rest.room;

import hotel.db.dto.room.ListIdRoomResponse;
import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.enums.BedType;
import hotel.db.enums.RoomStatus;
import hotel.db.enums.RoomType;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.db.repository.size.SizeRepository;
import hotel.service.booking.BookingService;
import hotel.service.file.RoomImageUploadService;
import hotel.service.image.ImageService;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/room")
public class RoomController {

	private final BookingService bookingService;
	private final RoomService roomService;
	private final FloorRepository floorRepository;
	private final SizeRepository sizeRepository;
	private final RoomImageUploadService fileUploadService;
	private final ImageService roomImageService;
	private final RoomImageRepository roomImageRepository;

    // Tự động load data cho dropdown trong mọi request
    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("roomTypes", RoomType.ALL);
        model.addAttribute("bedTypes", BedType.ALL);
        model.addAttribute("statuses", RoomStatus.ALL);
        model.addAttribute("floors", floorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("listRoom", roomService.getRoomList());
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
            System.out.println("=== START CREATE/UPDATE ROOM ===");
            System.out.println("Room ID: " + room.getRoomId());
            System.out.println("Room Number: " + room.getRoomNumber());
            System.out.println("Image Files: " + (imageFiles != null ? imageFiles.size() : 0));
            
            // Lưu số phòng cũ (để đổi tên folder nếu update)
            String oldRoomNumber = null;
            if (room.getRoomId() != null) {
                Room existingRoom = roomService.getRoomById(room.getRoomId());
                oldRoomNumber = existingRoom.getRoomNumber();
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

            System.out.println("=== SUCCESS - REDIRECTING ===");
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
        Room room =  roomService.getRoomById(id);
        if  (room == null) {
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
