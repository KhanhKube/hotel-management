package hotel.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public String getImageUrlAfterUpload(MultipartFile file) throws IOException {
        Map upload = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "Image"
                ));

        return (String) upload.get("url");
    }
}