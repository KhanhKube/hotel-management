package hotel.service.floor;

import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;

import java.util.List;

public interface FloorService {

    /**
     * Lấy tất cả floor đang hoạt động
     */
    List<FloorResponseDto> getAllActiveFloors();

    /**
     * Lấy floor theo ID
     */
    FloorResponseDto getFloorById(Long floorId);

    /**
     * Tạo floor mới
     */
    FloorResponseDto createFloor(FloorRequestDto floorRequestDto);

    /**
     * Cập nhật floor
     */
    FloorResponseDto updateFloor(Long floorId, FloorRequestDto floorRequestDto);

    /**
     * Xóa mềm floor
     */
    void deleteFloor(Long floorId);

    /**
     * Kiểm tra floor có tồn tại không
     */
    boolean existsById(Long floorId);

    /**
     * Kiểm tra số tầng đã tồn tại chưa
     */
    boolean existsByFloorNumber(Integer floorNumber);
}
