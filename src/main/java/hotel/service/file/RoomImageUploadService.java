package hotel.service.file;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomImageUploadService {

    @Value("${app.upload.dir}")
    private String uploadDir;


    public String uploadSingleRoomImage(String roomNumber, MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        // Validate file type - phải bắt đầu với "image/"
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File phải là ảnh (jpg, png, jpeg, webp)");
        }

        // Validate file size (tối đa 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("Kích thước ảnh không được vượt quá 10MB");
        }

        //Tạo path đường dẫn thư mục uploadDir/room/101/
        Path roomFolderPath = Paths.get(uploadDir, "room", roomNumber);

        //Tạo thư mục nếu chưa có
        if (!Files.exists(roomFolderPath)) {
            Files.createDirectories(roomFolderPath);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path filePath = roomFolderPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL: /images/room/101/abc-123.jpg
        return "/images/room/" + roomNumber + "/" + newFilename;
    }

    public List<String> uploadRoomImages(String roomNumber, List<MultipartFile> files) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String imageUrl = uploadSingleRoomImage(roomNumber, file);
                imageUrls.add(imageUrl);
            }
        }

        return imageUrls;
    }

    public void deleteRoomImage(String imageUrl) throws IOException {
        if (imageUrl != null && imageUrl.startsWith("/images/room/")) {
            // Lấy phần sau /images/: room/101/abc-123.jpg
            String relativePath = imageUrl.substring("/images/".length());
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        }
    }

    public void deleteRoomFolder(String roomNumber) throws IOException {
        Path roomFolderPath = Paths.get(uploadDir, "room", roomNumber);

        if (Files.exists(roomFolderPath)) {
            // Xóa tất cả file trong thư mục
            Files.walk(roomFolderPath)
                    .sorted((a, b) -> -a.compareTo(b)) // Xóa file trước, folder sau
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void renameRoomFolder(String oldRoomNumber, String newRoomNumber) throws IOException {
        Path oldPath = Paths.get(uploadDir, "room", oldRoomNumber);
        Path newPath = Paths.get(uploadDir, "room", newRoomNumber);

        if (Files.exists(oldPath)) {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}