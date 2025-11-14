package hotel.service.room;

import hotel.db.dto.furnishing.FurnishingFormDto;
import hotel.db.dto.room.*;
import hotel.db.entity.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public interface RoomService {
    
    List<FurnishingFormDto> getFurnishingsForForm(Integer roomId);
    
    void saveRoomFurnishings(Integer roomId, List<Integer> furnishingIds, List<Integer> quantities);

    void saveMaintenance(Integer roomId, String checkInDate, String checkOutDate,
                         String description, Integer createBy);

    List<String> getDateToDisableRoom(Integer roomId);

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



    HashMap<String, String> saveRoom(Room room);
    
    HashMap<String, String> createOrUpdateRoom(Room room, List<org.springframework.web.multipart.MultipartFile> imageFiles,
                                               List<Integer> furnishingIds, List<Integer> furnishingQuantities);

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
	
	void disableRoom(Integer roomId, String disableDate, String description, Integer createdBy);
}
