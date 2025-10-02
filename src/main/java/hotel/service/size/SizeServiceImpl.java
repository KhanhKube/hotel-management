package hotel.service.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.db.entity.Size;
import hotel.db.repository.size.SizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SizeResponseDto> getAllActiveSizes() {
        log.info("Getting all active sizes");
        List<Size> sizes = sizeRepository.findAllActive();
        return sizes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SizeResponseDto getSizeById(Long sizeId) {
        log.info("Getting size by ID: {}", sizeId);
        Size size = sizeRepository.findByIdActive(sizeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với ID: " + sizeId));
        return convertToResponseDto(size);
    }

    @Override
    public SizeResponseDto createSize(SizeRequestDto sizeRequestDto) {
        log.info("Creating new size with value: {}", sizeRequestDto.getSize());

        // Kiểm tra giá trị size đã tồn tại chưa
        if (sizeRepository.existsBySize(sizeRequestDto.getSize())) {
            throw new RuntimeException("Kích thước " + sizeRequestDto.getSize() + " đã tồn tại");
        }

        Size size = Size.builder()
                .size(sizeRequestDto.getSize())
                .build();
        size.setIsDeleted(false);

        Size savedSize = sizeRepository.save(size);
        log.info("Created size with ID: {}", savedSize.getSizeId());

        return convertToResponseDto(savedSize);
    }

    @Override
    public SizeResponseDto updateSize(Long sizeId, SizeRequestDto sizeRequestDto) {
        log.info("Updating size with ID: {}", sizeId);

        Size existingSize = sizeRepository.findByIdActive(sizeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với ID: " + sizeId));

        // Kiểm tra giá trị size đã tồn tại chưa (loại trừ size hiện tại)
        if (sizeRepository.existsBySizeAndIdNot(sizeRequestDto.getSize(), sizeId)) {
            throw new RuntimeException("Kích thước " + sizeRequestDto.getSize() + " đã tồn tại");
        }

        existingSize.setSize(sizeRequestDto.getSize());
        Size updatedSize = sizeRepository.save(existingSize);

        log.info("Updated size with ID: {}", updatedSize.getSizeId());
        return convertToResponseDto(updatedSize);
    }

    @Override
    public void deleteSize(Long sizeId) {
        log.info("Deleting size with ID: {}", sizeId);

        Size size = sizeRepository.findByIdActive(sizeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với ID: " + sizeId));

        size.setIsDeleted(true);
        sizeRepository.save(size);

        log.info("Deleted size with ID: {}", sizeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long sizeId) {
        return sizeRepository.findByIdActive(sizeId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySize(BigDecimal size) {
        return sizeRepository.existsBySize(size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SizeResponseDto> getSizesByRange(BigDecimal minSize, BigDecimal maxSize) {
        log.info("Getting sizes in range: {} - {}", minSize, maxSize);
        List<Size> sizes = sizeRepository.findBySizeRange(minSize, maxSize);
        return sizes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Size entity sang SizeResponseDto
     */
    private SizeResponseDto convertToResponseDto(Size size) {
        return SizeResponseDto.builder()
                .sizeId(size.getSizeId())
                .size(size.getSize())
                .createdAt(size.getCreatedAt())
                .updatedAt(size.getUpdatedAt())
                .build();
    }
}
