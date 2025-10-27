package hotel.service.room;

import hotel.db.dto.room.*;
import hotel.db.entity.Room;

import java.util.HashMap;
import java.util.List;

public interface RoomService {
    //Dùng cho hiện thông tin danh sách phòng phía Booking người dùng.
    List<RoomBookListDto> getRoomListForBooking();

    List<RoomListDto> getRoomList();

	List<Room> getAllRooms();

    boolean checkForCreateRoomNumber(String roomNumber);

    boolean checkForEditRoomNumber(String roomNumber, Long roomId);

    HashMap<String, String> saveRoom(Room room);

    void DeleteRoom(Integer id);

	ListRoomResponse getAllRoomForSearch();

	Room getRoomById(Integer roomId);

	Room createRoom(Room room);

	Room updateRoom(Integer roomId, Room room);

	// Hard delete method - xóa vĩnh viễn khỏi database
	void hardDeleteRoom(Integer roomId);


	// Check if room number exists
	boolean existsByRoomNumber(String roomNumber);

	List<RoomHomepageResponseDto> getTop3Rooms();

	RoomDetailResponseDto getRoomDetailById(Integer roomId);
}
