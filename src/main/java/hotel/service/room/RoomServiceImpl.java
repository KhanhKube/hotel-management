package hotel.service.room;

import hotel.db.dto.room.*;
import hotel.db.entity.Floor;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.entity.Size;
import hotel.db.enums.BedType;
import hotel.db.enums.RoomStatus;
import hotel.db.enums.RoomType;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.db.repository.size.SizeRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final FloorRepository floorRepository;

	private final RoomImageRepository roomImageRepository;

	private final SizeRepository sizeRepository;

    /*
    Trả về về List chứa các field cần thiết.
    */
    @Override
    public List<RoomListDto> getRoomList() {
        return roomRepository.findAllByIsDeletedFalse().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
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
    /*
    Lấy list các option enums của Status,Bedtype,RoomType
    */
    @Override
    public String[] getAllStatus() {
        return RoomStatus.ALL;
    }
    @Override
    public String[] getAllRoomTypes() {
        return RoomType.ALL;
    }
    @Override
    public String[] getAllBedTypes() {
        return BedType.ALL;
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
    public boolean saveRoom(Room room) {
        try {
            roomRepository.save(room);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    public void DeleteRoom(Integer id) {
        roomRepository.softDeleteById(id);
    }

    @Override
    private void validateRoomNumber(String roomNumber, Integer floorId) {
        // Lấy floor number từ floorId
        Integer floorNumber = floorRepository.findById(floorId)
                .map(floor -> floor.getFloorNumber())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng"));

        // Kiểm tra format số phòng (phải là số và có 3 chữ số)
        if (!roomNumber.matches("\\d{3}")) {
            throw new RuntimeException("Số phòng phải có đúng 3 chữ số (VD: 101, 205)");
        }

        // Lấy chữ số đầu tiên (floor number trong room number)
        int roomFloorNumber = Integer.parseInt(roomNumber.substring(0, 1));

        // Lấy 2 chữ số cuối (room number trong floor)
        int roomNumberInFloor = Integer.parseInt(roomNumber.substring(1));

        // Validate: Số đầu phải trùng với floor number
        if (roomFloorNumber != floorNumber) {
            throw new RuntimeException(
                    String.format("Số phòng phải bắt đầu bằng %d (tầng %d). VD: %d01, %d02, ..., %d10",
                            floorNumber, floorNumber, floorNumber, floorNumber, floorNumber)
            );
        }

        // Validate: 2 số cuối phải từ 01-10
        if (roomNumberInFloor < 1 || roomNumberInFloor > 10) {
            throw new RuntimeException(
                    String.format("Số phòng trong tầng phải từ 01-10. VD: %d01 đến %d10",
                            floorNumber, floorNumber)
            );
        }

        // Validate: Kiểm tra số lượng phòng trên tầng (không quá 10)
        long roomCountOnFloor = roomRepository.findAllByIsDeletedFalse().stream()
                .filter(r -> r.getFloorId().equals(floorId))
                .count();

        if (roomCountOnFloor >= 10) {
            throw new RuntimeException(
                    String.format("Tầng %d đã đủ 10 phòng. Không thể thêm phòng mới!", floorNumber)
            );
        }
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

		BigDecimal size = null;
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
