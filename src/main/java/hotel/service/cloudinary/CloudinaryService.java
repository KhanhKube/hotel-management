package hotel.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String getImageUrlAfterUpload(MultipartFile file) throws IOException;
}
