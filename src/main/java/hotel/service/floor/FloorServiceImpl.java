package hotel.service.floor;

import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;
import hotel.db.entity.Floor;
import hotel.db.repository.floor.FloorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FloorServiceImpl implements FloorService {

	private final FloorRepository floorRepository;

	@Override
	@Transactional(readOnly = true)
	public List<FloorResponseDto> getAllActiveFloors() {
		log.info("Getting all active floors");
		List<Floor> floors = floorRepository.findAllByIsDeletedIsFalse();
		return floors.stream()
				.map(this::convertToResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public FloorResponseDto getFloorById(Integer floorId) {
		log.info("Getting floor by ID: {}", floorId);
		Floor floor = floorRepository.findByFloorIdAndIsDeletedIsFalse(floorId);
		if (floor == null) {
			throw new RuntimeException("Floor with ID: " + floorId + " not found");
		}
		return convertToResponseDto(floor);
	}

	@Override
	public FloorResponseDto createFloor(FloorRequestDto floorRequestDto) {
		log.info("Creating new floor with number: {}", floorRequestDto.getFloorNumber());
		Boolean floorExits = floorRepository.existsByFloorNumberAndIsDeletedIsFalse(floorRequestDto.getFloorNumber());
		// Kiểm tra số tầng đã tồn tại chưa
		if (Boolean.TRUE.equals(floorExits)) {
			throw new RuntimeException("Số tầng " + floorRequestDto.getFloorNumber() + " đã tồn tại");
		}

		Floor floor = buildFloor(floorRequestDto);

		Floor savedFloor = floorRepository.save(floor);
		log.info("Created floor with ID: {}", savedFloor.getFloorId());

		return convertToResponseDto(savedFloor);
	}

	private Floor buildFloor(FloorRequestDto floorRequestDto) {
		Floor floor = new Floor();
		floor.setFloorNumber(floorRequestDto.getFloorNumber());
		floor.setIsDeleted(false);
		return floor;
	}

	@Override
	public FloorResponseDto updateFloor(Integer floorId, FloorRequestDto floorRequestDto) {
		log.info("Updating floor with ID: {}", floorId);

		Floor existingFloor = floorRepository.findByFloorIdAndIsDeletedIsFalse(floorId);

		if (existingFloor == null) {
			throw new RuntimeException("Floor with ID: " + floorId + " not found");
		}

		existingFloor.setFloorNumber(floorRequestDto.getFloorNumber());
		Floor updatedFloor = floorRepository.save(existingFloor);

		log.info("Updated floor with ID: {}", updatedFloor.getFloorId());
		return convertToResponseDto(updatedFloor);
	}

	@Override
	public void deleteFloor(Integer floorId) {
		log.info("Deleting floor with ID: {}", floorId);

		Floor floor = floorRepository.findByFloorIdAndIsDeletedIsFalse(floorId);
		if (floor == null) {
			throw new RuntimeException("Floor with ID: " + floorId + " not found");
		}

		floor.setIsDeleted(true);
		floorRepository.save(floor);

		log.info("Deleted floor with ID: {}", floorId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsById(Integer floorId) {
		Floor floor = floorRepository.findByFloorIdAndIsDeletedIsFalse(floorId);
		if (floor != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByFloorNumber(Integer floorNumber) {
		Floor floor = floorRepository.findByFloorIdAndIsDeletedIsFalse(floorNumber);
		if (floor != null) {
			return true;
		}
		return false;
	}

	/**
	 * Chuyển đổi Floor entity sang FloorResponseDto
	 */
	private FloorResponseDto convertToResponseDto(Floor floor) {
		return FloorResponseDto.builder()
				.floorId(floor.getFloorId())
				.floorNumber(floor.getFloorNumber())
				.createdAt(floor.getCreatedAt())
				.updatedAt(floor.getUpdatedAt())
				.build();
	}
}
