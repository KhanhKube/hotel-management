package hotel.rest.room;

import hotel.db.entity.Room;
//import hotel.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
//    private final RoomRepository roomRepo;
//
//    @GetMapping
//    public List<Room> list(@RequestParam(required = false) RoomStatus status) {
//        return status==null ? roomRepo.findAll() : roomRepo.findByStatus(status);
//    }
//
//    @PatchMapping("/{id}/status")
//    public Room changeStatus(@PathVariable Long id, @RequestParam RoomStatus status) {
//        Room r = roomRepo.findById(id).orElseThrow();
//        r.setStatus(status);
//        return roomRepo.save(r);
//    }
}
