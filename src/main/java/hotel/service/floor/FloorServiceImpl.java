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
        List<Floor> floors = floorRepository.findAllActive();
        return floors.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FloorResponseDto getFloorById(Long floorId) {
        log.info("Getting floor by ID: {}", floorId);
        Floor floor = floorRepository.findByIdActive(floorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng với ID: " + floorId));
        return convertToResponseDto(floor);
    }

    @Override
    public FloorResponseDto createFloor(FloorRequestDto floorRequestDto) {
        log.info("Creating new floor with number: {}", floorRequestDto.getFloorNumber());

        // Kiểm tra số tầng đã tồn tại chưa
        if (floorRepository.existsByFloorNumber(floorRequestDto.getFloorNumber())) {
            throw new RuntimeException("Số tầng " + floorRequestDto.getFloorNumber() + " đã tồn tại");
        }

        Floor floor = Floor.builder()
                .floorNumber(floorRequestDto.getFloorNumber())
                .isDeleted(false)   // ✅ sửa thành isDeleted
                .build();

        Floor savedFloor = floorRepository.save(floor);
        log.info("Created floor with ID: {}", savedFloor.getFloorId());

        return convertToResponseDto(savedFloor);
    }

    @Override
    public FloorResponseDto updateFloor(Long floorId, FloorRequestDto floorRequestDto) {
        log.info("Updating floor with ID: {}", floorId);

        Floor existingFloor = floorRepository.findByIdActive(floorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng với ID: " + floorId));

        // Kiểm tra số tầng đã tồn tại chưa (loại trừ floor hiện tại)
        if (floorRepository.existsByFloorNumberAndIdNot(floorRequestDto.getFloorNumber(), floorId)) {
            throw new RuntimeException("Số tầng " + floorRequestDto.getFloorNumber() + " đã tồn tại");
        }

        existingFloor.setFloorNumber(floorRequestDto.getFloorNumber());
        Floor updatedFloor = floorRepository.save(existingFloor);

        log.info("Updated floor with ID: {}", updatedFloor.getFloorId());
        return convertToResponseDto(updatedFloor);
    }

    @Override
    public void deleteFloor(Long floorId) {
        log.info("Deleting floor with ID: {}", floorId);

        Floor floor = floorRepository.findByIdActive(floorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng với ID: " + floorId));

        floor.setDeleted(true);   // ✅ sửa thành setDeleted
        floorRepository.save(floor);

        log.info("Deleted floor with ID: {}", floorId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long floorId) {
        return floorRepository.findByIdActive(floorId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFloorNumber(Integer floorNumber) {
        return floorRepository.existsByFloorNumber(floorNumber);
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
