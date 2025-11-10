package hotel.service.room;

import hotel.db.dto.room.*;
import hotel.db.entity.Floor;
import hotel.db.entity.Room;
import hotel.db.entity.Size;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface RoomService {

    //Lấy các ngày cho phép check-in
    List<String> getBookedDatesForBookingRoom(Integer roomId);

    //Lấy các ngày cho phép check-out
    List<String> getBookedDatesForCheckOut(Integer roomId);

    //Method filter dùng cho Room-BookingList bên phía customer.
    Page<RoomBookListDto> getRoomListWithFiltersAndPagination(BigDecimal minPrice, BigDecimal maxPrice,String roomType,
                                                              Integer floor, String bedType, String sortBy, int page, int size, String date);

    //Tăng view lên khi người dùng xem phòng.
    void incrementView(Integer roomId);

    List<Floor> getAllFloors();

    List<Size> getAllSizes();

    List<RoomListDto> getRoomList();
    
    // Method filter và pagination cho trang quản lý phòng (admin)
    Page<RoomListDto> getRoomListForManagement(String search, String roomType, String status, String systemstatus,
                                               Integer floor, Double size, BigDecimal minPrice, 
                                               BigDecimal maxPrice, String sortBy, int page, int pageSize);

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
