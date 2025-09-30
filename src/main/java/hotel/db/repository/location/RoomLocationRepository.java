package hotel.db.repository.location;

import hotel.db.entity.RoomLocation;
import hotel.repository.location.RoomLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class RoomLocationController {
    private final RoomLocationRepository repo;

    @GetMapping public List<RoomLocation> all() { return repo.findAll(); }
    @PostMapping public RoomLocation create(@RequestBody RoomLocation loc) { return repo.save(loc); }
    @PutMapping("/{id}")
    public RoomLocation update(@PathVariable Long id, @RequestBody RoomLocation loc) {
        RoomLocation old = repo.findById(id).orElseThrow();
        old.setBuilding(loc.getBuilding());
        old.setFloor(loc.getFloor());
        old.setDescription(loc.getDescription());
        return repo.save(old);
    }
    @PatchMapping("/{id}/status")
    public RoomLocation changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        RoomLocation loc = repo.findById(id).orElseThrow();
        loc.setActive(active);
        return repo.save(loc);
    }
}
