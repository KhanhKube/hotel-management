package hotel.service.floor;

import hotel.db.entity.Floor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface FloorService {
    List<Floor> getAllFloors();
    Floor getFloorById(Integer id);
    Floor saveFloor(Floor floor);
    void deleteFloor(Integer id);
}
