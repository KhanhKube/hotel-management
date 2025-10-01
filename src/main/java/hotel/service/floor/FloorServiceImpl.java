package hotel.service.floor;

import hotel.db.entity.Floor;
import hotel.db.repository.floor.FloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

    private final FloorRepository floorRepository;

    @Override
    public List<Floor> getAllFloors() {
        return floorRepository.findAll().stream()
                .filter(f -> Boolean.FALSE.equals(f.getIsDeleted()))
                .toList();
    }

    @Override
    public Floor getFloorById(Integer id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found"));
        if (Boolean.TRUE.equals(floor.getIsDeleted())) {
            throw new RuntimeException("Floor not found");
        }
        return floor;
    }

    @Override
    @Transactional
    public Floor saveFloor(Floor floor) {
        // Enforce DB constraint: floor_number NOT NULL
        if (floor.getFloorNumber() == null) {
            throw new IllegalArgumentException("Floor number is required");
        }
        return floorRepository.save(floor);
    }

    @Override
    @Transactional
    public void deleteFloor(Integer id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found"));
        floor.setIsDeleted(true);
        floorRepository.save(floor);
    }
}
