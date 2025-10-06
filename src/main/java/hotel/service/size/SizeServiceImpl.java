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
		List<Size> sizes = sizeRepository.findAllByIsDeletedIsFalse();
		return sizes.stream()
				.map(this::convertToResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public SizeResponseDto getSizeById(Integer sizeId) {
		log.info("Getting size by ID: {}", sizeId);
		Size size = sizeRepository.findBySizeIdAndIsDeletedIsFalse(sizeId);
		if (size == null) {
			throw new RuntimeException("Size not found");
		}
		return convertToResponseDto(size);
	}

	@Override
	public SizeResponseDto createSize(SizeRequestDto sizeRequestDto) {
		log.info("Creating new size with value: {}", sizeRequestDto.getSize());

		// Kiểm tra giá trị size đã tồn tại chưa
		if (sizeRepository.existsBySizeAndIsDeletedIsFalse(sizeRequestDto.getSize())) {
			throw new RuntimeException("Kích thước " + sizeRequestDto.getSize() + " đã tồn tại");
		}

		Size size = buildSize(sizeRequestDto);

		Size savedSize = sizeRepository.save(size);
		log.info("Created size with ID: {}", savedSize.getSizeId());

		return convertToResponseDto(savedSize);
	}

	private Size buildSize(SizeRequestDto sizeRequestDto) {
		Size size = new Size();
		size.setSize(sizeRequestDto.getSize());
		size.setIsDeleted(false);
		return size;

	}

	@Override
	public SizeResponseDto updateSize(Integer sizeId, SizeRequestDto sizeRequestDto) {
		log.info("Updating size with ID: {}", sizeId);

		Size existingSize = sizeRepository.findBySizeIdAndIsDeletedIsFalse(sizeId);
		if (existingSize == null) {
			throw new RuntimeException("Size not found");
		}

		// Kiểm tra giá trị size đã tồn tại chưa (loại trừ size hiện tại)
		if (sizeRepository.existsBySizeAndIsDeletedIsFalse(sizeRequestDto.getSize())) {
			throw new RuntimeException("Kích thước " + sizeRequestDto.getSize() + " đã tồn tại");
		}

		existingSize.setSize(sizeRequestDto.getSize());
		Size updatedSize = sizeRepository.save(existingSize);

		log.info("Updated size with ID: {}", updatedSize.getSizeId());
		return convertToResponseDto(updatedSize);
	}

	@Override
	public void deleteSize(Integer sizeId) {
		log.info("Deleting size with ID: {}", sizeId);

		Size size = sizeRepository.findBySizeIdAndIsDeletedIsFalse(sizeId);
		if (size == null) {
			throw new RuntimeException("Size not found");
		}

		size.setIsDeleted(true);
		sizeRepository.save(size);

		log.info("Deleted size with ID: {}", sizeId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsById(Integer sizeId) {
		Size size = sizeRepository.findBySizeIdAndIsDeletedIsFalse(sizeId);
		if (size == null) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsBySize(BigDecimal size) {
		return sizeRepository.existsBySizeAndIsDeletedIsFalse(size);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SizeResponseDto> getSizesByRange(BigDecimal minSize, BigDecimal maxSize) {
		log.info("Getting sizes in range: {} - {}", minSize, maxSize);
		List<Size> sizes = sizeRepository.findBySizeRangeAndIsDeletedIsFalse(minSize, maxSize);
		return sizes.stream()
				.map(this::convertToResponseDto)
				.collect(Collectors.toList());
	}


	private SizeResponseDto convertToResponseDto(Size size) {
		return SizeResponseDto.builder()
				.sizeId(size.getSizeId())
				.size(size.getSize())
				.createdAt(size.getCreatedAt())
				.updatedAt(size.getUpdatedAt())
				.build();
	}
}
