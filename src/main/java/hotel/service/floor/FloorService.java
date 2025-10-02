package hotel.service.floor;

import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;

import java.util.List;

public interface FloorService {


	List<FloorResponseDto> getAllActiveFloors();

	FloorResponseDto getFloorById(Integer floorId);

	FloorResponseDto createFloor(FloorRequestDto floorRequestDto);

	FloorResponseDto updateFloor(Integer floorId, FloorRequestDto floorRequestDto);

	void deleteFloor(Integer floorId);

	boolean existsById(Integer floorId);

	boolean existsByFloorNumber(Integer floorNumber);
}
