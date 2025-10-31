package hotel.service.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.db.entity.Size;
import hotel.db.repository.size.SizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;

    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    @Override
    public List<SizeResponseDto> getAllActiveSizes() {
        return sizeRepository.findAll().stream()
                .filter(size -> !Boolean.TRUE.equals(size.getIsDeleted()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Size getSizeById(Integer id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found with ID: " + id));
    }

    @Override
    public SizeResponseDto getSizeByIdDto(Integer id) {
        Size size = getSizeById(id);
        return convertToDto(size);
    }

    @Override
    @Transactional
    public SizeResponseDto createSize(SizeRequestDto sizeRequestDto) {
        Size size = new Size();
        size.setSize(sizeRequestDto.getSize());
        Size savedSize = saveSize(size);
        return convertToDto(savedSize);
    }

    @Override
    @Transactional
    public SizeResponseDto updateSize(Integer id, SizeRequestDto sizeRequestDto) {
        Size existingSize = getSizeById(id);
        existingSize.setSize(sizeRequestDto.getSize());
        Size savedSize = saveSize(existingSize);
        return convertToDto(savedSize);
    }

    @Override
    @Transactional
    public Size saveSize(Size size) {
        // Enforce DB constraint: size NOT NULL and > 0
        if (size.getSize() == null || size.getSize() <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        
        // Check for duplicate size only for new sizes
        if (size.getSizeId() == null && existsBySize(size.getSize())) {
            throw new IllegalArgumentException("Kích thước " + size.getSize() + "m² đã tồn tại!");
        }
        
        // For existing sizes, check if the new size conflicts with other sizes
        if (size.getSizeId() != null) {
            List<Size> existingSizes = sizeRepository.findBySizeAndSizeIdNot(size.getSize(), size.getSizeId());
            if (!existingSizes.isEmpty()) {
                throw new IllegalArgumentException("Kích thước " + size.getSize() + "m² đã tồn tại!");
            }
        }
        
        return sizeRepository.save(size);
    }

    @Override
    @Transactional
    public void deleteSize(Integer id) {
        if (!sizeRepository.existsById(id)) {
            throw new RuntimeException("Size not found with ID: " + id);
        }
        sizeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return sizeRepository.existsById(id);
    }

    @Override
    public boolean existsBySize(Double size) {
        return sizeRepository.existsBySize(size);
    }

    @Override
    public List<SizeResponseDto> getSizesByRange(Double minSize, Double maxSize) {
        return sizeRepository.findAll().stream()
                .filter(size -> !Boolean.TRUE.equals(size.getIsDeleted()))
                .filter(size -> size.getSize() >= minSize && size.getSize() <= maxSize)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SizeResponseDto convertToDto(Size size) {
        return SizeResponseDto.builder()
                .sizeId(size.getSizeId())
                .size(size.getSize())
                .createdAt(size.getCreatedAt())
                .updatedAt(size.getUpdatedAt())
                .isDeleted(size.getIsDeleted())
                .build();
    }
}