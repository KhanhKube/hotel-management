package hotel.service.size;

import hotel.db.entity.Size;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface SizeService {
    List<Size> getAllSizes();
    Size getSizeById(Integer id);
    Size saveSize(Size size);
    void deleteSize(Integer id);
}
