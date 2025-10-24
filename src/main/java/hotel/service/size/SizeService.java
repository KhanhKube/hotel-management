package hotel.service.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface SizeService {

	List<SizeResponseDto> getAllActiveSizes();

	SizeResponseDto getSizeById(Integer sizeId);

	SizeResponseDto createSize(SizeRequestDto sizeRequestDto);

	SizeResponseDto updateSize(Integer sizeId, SizeRequestDto sizeRequestDto);

	void deleteSize(Integer sizeId);

	boolean existsById(Integer sizeId);

	boolean existsBySize(Double size);

	List<SizeResponseDto> getSizesByRange(Double minSize, Double maxSize);
}
