package hotel.service.size;

import hotel.db.entity.Size;
import hotel.db.repository.size.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;

    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll().stream()
                .filter(s -> Boolean.FALSE.equals(s.getIsDeleted()))
                .toList();
    }

    @Override
    public Size getSizeById(Integer id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found"));
        if (Boolean.TRUE.equals(size.getIsDeleted())) {
            throw new RuntimeException("Size not found");
        }
        return size;
    }

    @Override
    @Transactional
    public Size saveSize(Size size) {
        // Enforce DB constraint: size NOT NULL and precision <= 5, scale <= 2
        if (size.getSize() == null) {
            throw new IllegalArgumentException("Size is required");
        }
        return sizeRepository.save(size);
    }

    @Override
    @Transactional
    public void deleteSize(Integer id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found"));
        size.setIsDeleted(true);
        sizeRepository.save(size);
    }
}
