package hotel.rest.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.service.size.SizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class SizeController {

    private final SizeService sizeService;

    /**
     * Lấy tất cả size đang hoạt động
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSizes() {
        try {
            List<SizeResponseDto> sizes = sizeService.getAllActiveSizes();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách kích thước thành công");
            response.put("data", sizes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all sizes", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy size theo ID
     */
    @GetMapping("/{sizeId}")
    public ResponseEntity<Map<String, Object>> getSizeById(@PathVariable Long sizeId) {
        try {
            SizeResponseDto size = sizeService.getSizeById(sizeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy thông tin kích thước thành công");
            response.put("data", size);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Size not found with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting size by ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tạo size mới
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSize(@Valid @RequestBody SizeRequestDto sizeRequestDto) {
        try {
            SizeResponseDto createdSize = sizeService.createSize(sizeRequestDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo kích thước mới thành công");
            response.put("data", createdSize);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error creating size", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error creating size", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tạo kích thước mới: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật size
     */
    @PutMapping("/{sizeId}")
    public ResponseEntity<Map<String, Object>> updateSize(
            @PathVariable Long sizeId,
            @Valid @RequestBody SizeRequestDto sizeRequestDto) {
        try {
            SizeResponseDto updatedSize = sizeService.updateSize(sizeId, sizeRequestDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật kích thước thành công");
            response.put("data", updatedSize);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating size with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error updating size with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa size (soft delete)
     */
    @DeleteMapping("/{sizeId}")
    public ResponseEntity<Map<String, Object>> deleteSize(@PathVariable Long sizeId) {
        try {
            sizeService.deleteSize(sizeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa kích thước thành công");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error deleting size with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error deleting size with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Kiểm tra size có tồn tại không
     */
    @GetMapping("/{sizeId}/exists")
    public ResponseEntity<Map<String, Object>> checkSizeExists(@PathVariable Long sizeId) {
        try {
            boolean exists = sizeService.existsById(sizeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kiểm tra kích thước thành công");
            response.put("data", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking size existence with ID: {}", sizeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi kiểm tra kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Kiểm tra giá trị size đã tồn tại chưa
     */
    @GetMapping("/check-size/{size}")
    public ResponseEntity<Map<String, Object>> checkSizeValueExists(@PathVariable BigDecimal size) {
        try {
            boolean exists = sizeService.existsBySize(size);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kiểm tra giá trị kích thước thành công");
            response.put("data", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking size value existence: {}", size, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi kiểm tra giá trị kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tìm size trong khoảng giá trị
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getSizesByRange(
            @RequestParam BigDecimal minSize,
            @RequestParam BigDecimal maxSize) {
        try {
            List<SizeResponseDto> sizes = sizeService.getSizesByRange(minSize, maxSize);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách kích thước trong khoảng thành công");
            response.put("data", sizes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting sizes by range: {} - {}", minSize, maxSize, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách kích thước: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
