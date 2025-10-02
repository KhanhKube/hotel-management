package hotel.rest.floor;

import hotel.db.dto.floor.FloorRequestDto;
import hotel.db.dto.floor.FloorResponseDto;
import hotel.service.floor.FloorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
@Slf4j
@Validated
public class FloorController {

	private final FloorService floorService;

	/**
	 * Lấy tất cả floor đang hoạt động
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllFloors() {
		try {
			List<FloorResponseDto> floors = floorService.getAllActiveFloors();
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Lấy danh sách tầng thành công");
			response.put("data", floors);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error getting all floors", e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi lấy danh sách tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Lấy floor theo ID
	 */
	@GetMapping("/{floorId}")
	public ResponseEntity<Map<String, Object>> getFloorById(@PathVariable Integer floorId) {
		try {
			FloorResponseDto floor = floorService.getFloorById(floorId);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Lấy thông tin tầng thành công");
			response.put("data", floor);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("Floor not found with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		} catch (Exception e) {
			log.error("Error getting floor by ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi lấy thông tin tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Tạo floor mới
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> createFloor(@Valid @RequestBody FloorRequestDto floorRequestDto) {
		try {
			FloorResponseDto createdFloor = floorService.createFloor(floorRequestDto);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Tạo tầng mới thành công");
			response.put("data", createdFloor);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (RuntimeException e) {
			log.error("Error creating floor", e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			log.error("Error creating floor", e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi tạo tầng mới: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Cập nhật floor
	 */
	@PutMapping("/{floorId}")
	public ResponseEntity<Map<String, Object>> updateFloor(
			@PathVariable Integer floorId,
			@Valid @RequestBody FloorRequestDto floorRequestDto) {
		try {
			FloorResponseDto updatedFloor = floorService.updateFloor(floorId, floorRequestDto);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Cập nhật tầng thành công");
			response.put("data", updatedFloor);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("Error updating floor with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			log.error("Error updating floor with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi cập nhật tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Xóa floor (soft delete)
	 */
	@DeleteMapping("/{floorId}")
	public ResponseEntity<Map<String, Object>> deleteFloor(@PathVariable Integer floorId) {
		try {
			floorService.deleteFloor(floorId);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Xóa tầng thành công");
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("Error deleting floor with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		} catch (Exception e) {
			log.error("Error deleting floor with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi xóa tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Kiểm tra floor có tồn tại không
	 */
	@GetMapping("/{floorId}/exists")
	public ResponseEntity<Map<String, Object>> checkFloorExists(@PathVariable Integer floorId) {
		try {
			boolean exists = floorService.existsById(floorId);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Kiểm tra tầng thành công");
			response.put("data", exists);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error checking floor existence with ID: {}", floorId, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi kiểm tra tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * Kiểm tra số tầng đã tồn tại chưa
	 */
	@GetMapping("/check-floor-number/{floorNumber}")
	public ResponseEntity<Map<String, Object>> checkFloorNumberExists(@PathVariable Integer floorNumber) {
		try {
			boolean exists = floorService.existsByFloorNumber(floorNumber);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Kiểm tra số tầng thành công");
			response.put("data", exists);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error checking floor number existence: {}", floorNumber, e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi kiểm tra số tầng: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}