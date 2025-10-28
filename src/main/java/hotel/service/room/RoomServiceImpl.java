package hotel.service.room;

import hotel.db.dto.room.*;
import hotel.db.entity.Floor;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.entity.Size;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.db.repository.size.SizeRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final FloorRepository floorRepository;
    private final OrderDetailRepository orderDetailRepository;
	private final RoomImageRepository roomImageRepository;

	private final SizeRepository sizeRepository;

    @Override
    public Page<RoomBookListDto> getRoomListWithFiltersAndPagination(BigDecimal minPrice, BigDecimal maxPrice, String roomType,
                                                                     Integer floor, String bedType, String sortBy, int page, int size) {
        List<Room> rooms = roomRepository.findAll();

        //Filter theo roomType (Loại phòng)
        if (roomType != null && !roomType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x->x.getRoomType().equals(roomType))
                    .collect(Collectors.toList());
        }
        //Filter theo giá min
        if (minPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        //Filter theo giá trần
        if (maxPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        //Filter theo tầng
        if (floor != null) {
            rooms = rooms.stream()
                    .filter(x -> floor.equals(x.getFloorId()))
                    .collect(Collectors.toList());
        }
        //Filter theo bedtype
        if (bedType != null && !bedType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> bedType.equals(x.getBedType()))
                    .collect(Collectors.toList());
        }
        //Thêm đk Sort
        if (sortBy != null) {
            switch (sortBy) {
                case "price-asc":
                    rooms.sort(Comparator.comparing(Room::getPrice));
                    break;
                case "price-desc":
                    rooms.sort(Comparator.comparing(Room::getPrice).reversed());
                    break;
                case "room-asc":
                    rooms.sort(Comparator.comparing(Room::getRoomNumber));
                    break;
                case "room-desc":
                    rooms.sort(Comparator.comparing(Room::getRoomNumber).reversed());
                    break;
            }
        }
        List <RoomBookListDto> BookList = rooms.stream() //Lấy Listcác phòng đã được lọc field qua Dto
                .filter(x -> !"Bảo trì".equals(x.getStatus()))
                .map(x -> {
                    return toRoomBookDto(x);
                }).collect(Collectors.toList());

        //Paging thủ công
        int startpage = page*size;
        int endpage = Math.min(startpage + size, BookList.size());

        List<RoomBookListDto> pageDtos = BookList.subList(startpage, endpage);

        return new PageImpl<>(pageDtos, PageRequest.of(page, size), BookList.size());
    }

    /*
    //Dto trả về field cho trang hiển thị danh sách phòng cho bên customer booking
    */
    private RoomBookListDto toRoomBookDto(Room x) {
        RoomBookListDto dto = new RoomBookListDto();
        dto.setRoomId(x.getRoomId());
        dto.setRoomNumber(x.getRoomNumber());
        dto.setRoomType(x.getRoomType());
        dto.setPrice(x.getPrice());
        dto.setRoomDescription(x.getRoomDescription());

        //Lấy ảnh đầu tiên của phòng theo id
        List<RoomImage> roomImages = roomImageRepository.findByRoomId(x.getRoomId());
        if (!roomImages.isEmpty()) {
            dto.setImageRoom(roomImages.get(0).getRoomImageUrl()); //Lấy url của ảnh đầu tiên (index 0)
        } else {
            dto.setImageRoom("https://images.pexels.com/photos/261102/pexels-photo-261102.jpeg"); //lấy ảnh mạng
        }

        //Logic nghiệp vụ cho Status phòng VIPVIPVIPV
        if ("Trống".equals(x.getStatus())) {
            dto.setStatusDisplay("Trống");;
            dto.setAvailableFrom(null);
        } else {
            LocalDate nextAvailable = orderDetailRepository.findNextAvailableDateByRoomId(x.getRoomId());
            if (nextAvailable != null) {
                dto.setStatusDisplay("Có thể đặt từ " +
                        nextAvailable.plusDays(1/12)
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // có thể đặt tiếp phòng sau 2 tiếng enddate của phòng cũ
                dto.setAvailableFrom(nextAvailable.plusDays(1/12)); // tăng thêm 2 tiếng.
            } else {
                //TH query trả về null.
                dto.setStatusDisplay("Trống");
                dto.setAvailableFrom(null);
            }
        }

        return dto;
    }

    /*
    Listtrả về về List chứa các field cần thiết.
    */
    @Override
    public List<RoomListDto> getRoomList() {
        return roomRepository.findAllByIsDeletedFalse().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }
    
    /*
    Method filter và pagination cho trang quản lý phòng (admin)
    */
    @Override
    public Page<RoomListDto> getRoomListForManagement(String search, String roomType, String status,
                                                      Integer floor, Double size, BigDecimal minPrice,
                                                      BigDecimal maxPrice, String sortBy, int page, int pageSize) {
        List<Room> rooms = roomRepository.findAllByIsDeletedFalse();
        
        // Filter theo search (số phòng)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            rooms = rooms.stream()
                    .filter(x -> x.getRoomNumber().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        
        // Filter theo loại phòng
        if (roomType != null && !roomType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getRoomType().equals(roomType))
                    .collect(Collectors.toList());
        }
        
        // Filter theo trạng thái
        if (status != null && !status.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        // Filter theo tầng
        if (floor != null) {
            rooms = rooms.stream()
                    .filter(x -> {
                        Integer roomFloorId = x.getFloorId();
                        if (roomFloorId == null) return false;
                        return floorRepository.findById(roomFloorId)
                                .map(f -> f.getFloorNumber().equals(floor))
                                .orElse(false);
                    })
                    .collect(Collectors.toList());
        }
        
        // Filter theo diện tích
        if (size != null) {
            rooms = rooms.stream()
                    .filter(x -> {
                        Integer roomSizeId = x.getSizeId();
                        if (roomSizeId == null) return false;
                        return sizeRepository.findById(roomSizeId)
                                .map(s -> s.getSize().equals(size))
                                .orElse(false);
                    })
                    .collect(Collectors.toList());
        }
        
        // Filter theo giá min
        if (minPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        
        // Filter theo giá max
        if (maxPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        
        // Sort
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortParams = sortBy.split(",");
            String field = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";
            
            Comparator<Room> comparator = null;
            
            if ("roomNumber".equals(field)) {
                comparator = Comparator.comparing(Room::getRoomNumber);
            } else if ("price".equals(field)) {
                comparator = Comparator.comparing(Room::getPrice);
            }
            
            if (comparator != null) {
                if ("desc".equals(direction)) {
                    comparator = comparator.reversed();
                }
                rooms.sort(comparator);
            }
        }
        
        // Convert to DTO
        List<RoomListDto> roomDtos = rooms.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
        
        // Pagination thủ công
        int startPage = page * pageSize;
        int endPage = Math.min(startPage + pageSize, roomDtos.size());
        
        List<RoomListDto> pagedDtos = roomDtos.subList(startPage, endPage);
        
        return new PageImpl<>(pagedDtos, PageRequest.of(page, pageSize), roomDtos.size());
    }

    private RoomListDto toListDto(Room room) {
        // Query floor number từ repository
        Integer floorNumber = null;
        if (room.getFloorId() != null) {
            floorNumber = floorRepository.findById(room.getFloorId())
                    .map(floor -> floor.getFloorNumber())
                    .orElse(null);
        }

        // Query size từ repository
        Double sizeValue = null;
        if (room.getSizeId() != null) {
            sizeValue = sizeRepository.findById(room.getSizeId())
                    .map(size -> size.getSize().doubleValue())
                    .orElse(null);
        }

        return new RoomListDto(
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType(),
                floorNumber,
                sizeValue,
                room.getPrice(),
                room.getStatus()
        );
    }

    @Override
    public boolean checkForCreateRoomNumber(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }
    @Override
    public boolean checkForEditRoomNumber(String roomNumber, Long roomId) {
        return roomRepository.existsByRoomNumberAndRoomId(roomNumber, roomId);
    }
    @Override
    public HashMap<String, String> saveRoom(Room room) {
        HashMap<String, String> result = new HashMap<>();

        try {
            // Validate - truyền thêm roomId để phân biệt CREATE vs UPDATE
            String validationError = validateRoomNumber(room.getRoomNumber(), room.getFloorId(), room.getSizeId(), room.getRoomType(), room.getBedType(), room.getRoomId());
            if (validationError != "") {
                result.put("error", validationError);
                return result;
            }

            roomRepository.save(room);
            result.put("success", "Thành công");
            return result;

        } catch (Exception e) {
            log.error("Error saving room: {}", e.getMessage());
            result.put("error", e.getMessage());
            return result;
        }
    }
    @Override
    public void DeleteRoom(Integer id) {
        roomRepository.softDeleteById(id);
    }

    private String validateRoomNumber(String roomNumber, Integer floorId, Integer sizeId, String roomType, String bedType, Integer roomId) {
        //lấy số tầng và số size.
        Integer floorNumber = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng"))
                .getFloorNumber();

        Double sizeNumber = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng"))
                .getSize();

        if (roomType.equals("Tiêu chuẩn")) {
            if (sizeNumber > 40) {
                return "Phòng Tiêu chuẩn diện tích vui lòng nhỏ hơn hoặc bằng 40 mét vuông!";
            }
            if (bedType.equals("Giường King") || bedType.equals("Giường Queen")) {
                return "Phong tiêu chuẩn chỉ được chọn giường Đơn hoặc Đôi!";
            }
        } else if (roomType.equals("Cao cấp") || roomType.equals("Hạng sang")) {
            if (sizeNumber > 50 || sizeNumber <= 40) {
                return "Phòng "+roomType+" diện tích vui lòng nhỏ hơn hoặc bằng " +
                        "50 và lớn hơn 40 mét vuông!";
            }
        } else if (roomType.equals("VIP")) {
            if (sizeNumber > 70 || sizeNumber <= 50) {
                return "Phòng "+roomType+" diện tích vui lòng nhỏ hơn hoặc bằng " +
                        "70 và lớn hơn 50 mét vuông!";
            }
        } else {
            if (sizeNumber < 80) {
                return "Phòng "+roomType+" diện tích vui lòng lớn hơn hoặc bằng 80 mét vuông!";
            }
            if (!bedType.equals("Giường King") && !bedType.equals("Giường Queen")) {
                return "Phòng Tổng thống vui lòng chọn giường King hoặc Queen!";
            }
        }

        // kiểm tra format số phòng
        if (!roomNumber.matches("\\d{3}")) {
            return "Số phòng phải có đúng 3 chữ số (VD: 101, 205). Vui lòng nhập lại!";
        }

        //Lấy chữ số đầu tiên
        int roomFloorNumber = Integer.parseInt(roomNumber.substring(0, 1));

        //lấy các số còn lại
        int roomNumberInFloor = Integer.parseInt(roomNumber.substring(1));

        // Số đâù tiên phải là số tầng
        if (roomFloorNumber != floorNumber) {
            return "Chữ số đầu tiên phải trùng với số tầng. Vui lòng nhập lại!";
        }

        // 2 số cuối từ 1-11
        if (roomNumberInFloor < 1 || roomNumberInFloor > 11) {
            return "Vui lòng nhập số phòng từ 0-10!";
        }

        // Đếm các phòng chưa bị xóa theo tầng
        // Nếu là UPDATE (roomId != null), loại trừ phòng hiện tại khỏi count
        long roomCountOnFloor = roomRepository.findAllByIsDeletedFalse().stream()
                .filter(r -> r.getFloorId().equals(floorId))
                .filter(r -> roomId == null || !r.getRoomId().equals(roomId)) // Loại trừ phòng đang update
                .count();
        // Lớn hơn 11 trả về message error
        if (roomCountOnFloor >= 11) {
            return "Số lượng phòng trên tầng "
                    + floorNumber + " đã đầy!";
        }
        return "";
    }
    @Override
    public void incrementView(Integer roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            Integer currentView = room.getView() != null ? room.getView() : 0;
            room.setView(currentView + 1);
            roomRepository.save(room);
        }
    }

    @Override
    public List<Floor> getAllFloors() {
        return floorRepository.findAll();
    }

    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }


    @Override
	@NotNull
	public List<Room> getAllRooms() {
		log.info("Getting all rooms");
		return roomRepository.findAll();
	}

	@Override
	public ListRoomResponse getAllRoomForSearch() {
		List<Room> rooms = roomRepository.findAllByIsDeletedIsFalse();
		return buildListRoomResponse(rooms);
	}

	public ListRoomResponse buildListRoomResponse(List<Room> rooms) {
		if (rooms == null || rooms.isEmpty()) {
			return new ListRoomResponse(Collections.emptyList());
		}

		List<RoomResponseDto> roomDtos = rooms.stream()
				.map(this::buildRoomResponse)
				.collect(Collectors.toList());

		return new ListRoomResponse(roomDtos);
	}

	private RoomResponseDto buildRoomResponse(Room room) {
		if (room == null) return null;

		// Query floor và size từ repository
		Integer floorNumber = null;
		if (room.getFloorId() != null) {
			floorNumber = floorRepository.findById(room.getFloorId())
					.map(floor -> floor.getFloorNumber())
					.orElse(null);
		}

		Double size = null;
		if (room.getSizeId() != null) {
			size = sizeRepository.findById(room.getSizeId())
					.map(s -> s.getSize())
					.orElse(null);
		}

		return RoomResponseDto.builder()
				.roomId(room.getRoomId() != null ? room.getRoomId().longValue() : null)
				.roomNumber(room.getRoomNumber())
				.roomType(room.getRoomType())
				.bedType(room.getBedType())
				.floorNumber(floorNumber)
				.size(size)
				.roomDescription(room.getRoomDescription())
				.price(room.getPrice())
				.status(room.getStatus())
				.sold(room.getSold())
				.view(room.getView())
				.createdAt(room.getCreatedAt())
				.updatedAt(room.getUpdatedAt())
				.isDeleted(room.getIsDeleted())
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public Room getRoomById(Integer roomId) {
		log.info("Getting room by ID: {}", roomId);
		return roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
	}

	@Override
	public Room createRoom(Room room) {
		log.info("Creating new room: {}", room.getRoomNumber());

		// Kiểm tra room number có trùng không
		if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
			throw new RuntimeException("Phòng " + room.getRoomNumber() + " đã tồn tại. Vui lòng nhập số phòng khác.");
		}

		Room savedRoom = roomRepository.save(room);
		log.info("Created room with ID: {}", savedRoom.getRoomId());
		return savedRoom;
	}

	@Override
	public Room updateRoom(Integer roomId, Room room) {
		log.info("Updating room with ID: {}", roomId);

		Room existingRoom = getRoomById(roomId);

		// Kiểm tra room number có trùng không (trừ phòng hiện tại)
		if (!existingRoom.getRoomNumber().equals(room.getRoomNumber()) &&
				roomRepository.existsByRoomNumber(room.getRoomNumber())) {
			throw new RuntimeException("Phòng " + room.getRoomNumber() + " đã tồn tại. Vui lòng nhập số phòng khác.");
		}

		existingRoom.setRoomNumber(room.getRoomNumber());
		existingRoom.setRoomType(room.getRoomType());
		existingRoom.setBedType(room.getBedType());
		existingRoom.setFloorId(room.getFloorId());
		existingRoom.setSizeId(room.getSizeId());
		existingRoom.setPrice(room.getPrice());
		existingRoom.setStatus(room.getStatus());
		existingRoom.setRoomDescription(room.getRoomDescription());
		existingRoom.setSold(room.getSold() != null ? room.getSold() : existingRoom.getSold());
		existingRoom.setView(room.getView() != null ? room.getView() : existingRoom.getView());

		Room updatedRoom = roomRepository.save(existingRoom);
		log.info("Updated room with ID: {}", updatedRoom.getRoomId());
		return updatedRoom;
	}

	@Override
	@Transactional
	public void hardDeleteRoom(Integer roomId) {
		log.info("Hard deleting room with ID: {}", roomId);

		// Kiểm tra phòng có tồn tại không
		getRoomById(roomId);

		// Xóa các bản ghi liên quan trước khi xóa room
		// Xóa room views
		roomRepository.deleteRoomViewsByRoomId(roomId);

		// Xóa room furnishings
		roomRepository.deleteRoomFurnishingsByRoomId(roomId);

		// Xóa room images
		roomRepository.deleteRoomImagesByRoomId(roomId);

		// Xóa order details liên quan đến room này
		roomRepository.deleteOrderDetailsByRoomId(roomId);

		// Cuối cùng xóa room
		roomRepository.hardDeleteRoom(roomId);
		log.info("Hard deleted room with ID: {}", roomId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByRoomNumber(String roomNumber) {
		log.info("Checking if room number exists: {}", roomNumber);
		return roomRepository.existsByRoomNumber(roomNumber);
	}

	@Override
	public List<RoomHomepageResponseDto> getTop3Rooms() {
		List<Room> rooms = roomRepository.findTop3ByOrderBySoldDesc();
		List<RoomHomepageResponseDto> results = new ArrayList<>();

		for (Room room : rooms) {
			RoomHomepageResponseDto dto = new RoomHomepageResponseDto();
			dto.setRoomId(room.getRoomId());
			dto.setPrice(room.getPrice());
			dto.setRoomType(room.getRoomType());
			dto.setRoomDescription(room.getRoomDescription());

			List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getRoomId());
			if (roomImages != null && !roomImages.isEmpty()) {
				dto.setImageRoom(roomImages.get(0).getRoomImageUrl());
			}

			results.add(dto);
		}

		return results;
	}


	@Override
	public RoomDetailResponseDto getRoomDetailById(Integer roomId) {
		Room room = roomRepository.findByRoomId(roomId);
		if (room == null) {
			return null;
		}

		// Lấy thông tin kích thước phòng (size)
		Size size = sizeRepository.findBySizeIdAndIsDeletedIsFalse(room.getSizeId());

		// Tạo DTO trả về
		RoomDetailResponseDto dto = new RoomDetailResponseDto();
		dto.setRoomId(room.getRoomId());
		dto.setRoomNumber(room.getRoomNumber());
		dto.setRoomType(room.getRoomType());
		dto.setBedType(room.getBedType());
		dto.setFloorNumber(room.getFloorId());
		dto.setSize(size != null ? size.getSize() : null);
		dto.setPrice(room.getPrice());
		dto.setStatus(room.getStatus());
		dto.setSold(room.getSold());
		dto.setView(room.getView());
		dto.setRoomDescription(room.getRoomDescription());

		List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getRoomId());
		List<String> imageUrls = new ArrayList<>();

		if (roomImages != null && !roomImages.isEmpty()) {
			for (RoomImage roomImage : roomImages) {
				imageUrls.add(roomImage.getRoomImageUrl());
			}
		}

		dto.setImages(imageUrls);

		return dto;
	}
}
