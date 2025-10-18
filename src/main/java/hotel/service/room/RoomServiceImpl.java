package hotel.service.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.RoomHomepageResponseDto;
import hotel.db.dto.room.RoomListDto;
import hotel.db.dto.room.RoomResponseDto;
import hotel.db.entity.Floor;
import hotel.db.entity.Room;
import hotel.db.entity.RoomImage;
import hotel.db.entity.Size;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.db.repository.size.SizeRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final FloorRepository floorRepository;
	private final SizeRepository sizeRepository;
	private final RoomImageRepository roomImageRepository;

	//Trả về về List chứa các field cần thiết.
	public List<RoomListDto> getRoomList() {
		return roomRepository.findAll().stream()
				.map(this::toListDto)
				.collect(Collectors.toList());
	}

	private RoomListDto toListDto(Room room) {
		Double sizeValue = room.getSize() != null ? room.getSize().getSize().doubleValue() : null;

		return new RoomListDto(
				room.getRoomId(),
				room.getRoomNumber(),
				room.getRoomType(),
				room.getFloorId(),
				sizeValue,
				room.getPrice(),
				room.getStatus()
		);
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

		Floor floor = floorRepository.findById(room.getFloorId()).orElse(null);
		Size size = sizeRepository.findById(room.getSizeId()).orElse(null);
		if (floor == null) return null;
		if (size == null) return null;
		return RoomResponseDto.builder()
				.roomId(room.getRoomId() != null ? room.getRoomId().longValue() : null)
				.roomNumber(room.getRoomNumber())
				.roomType(room.getRoomType())
				.bedType(room.getBedType())
				.floorNumber(floor.getFloorNumber())
				.size(size.getSize())
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
			List<RoomImage> roomImage = roomImageRepository.findByRoomId(room.getRoomId());
			if (roomImage != null && !roomImage.isEmpty()) {
				dto.setImageRoom(roomImage.get(0).getRoomImageUrl());
			}
			dto.setRoomId(room.getRoomId());
			dto.setPrice(room.getPrice());
			dto.setRoomType(room.getRoomType());
			dto.setRoomDescription(room.getRoomDescription());
			results.add(dto);
		}
		return results;
	}
}
