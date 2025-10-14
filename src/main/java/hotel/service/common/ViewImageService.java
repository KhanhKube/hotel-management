package hotel.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@Slf4j
public class ViewImageService {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/views/";
    private static final String UPLOAD_URL = "/uploads/views/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    // Map để lưu trữ mapping giữa viewId và imagePath
    private final Map<Integer, String> viewImageMap = new ConcurrentHashMap<>();

    public String uploadImageForView(Integer viewId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidExtension(originalFilename)) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file ảnh: JPG, JPEG, PNG, GIF, WEBP");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename with viewId prefix
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "view_" + viewId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Delete old image if exists
        String oldImagePath = viewImageMap.get(viewId);
        if (oldImagePath != null) {
            deleteImageFile(oldImagePath);
        }

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL path
        String fileUrl = UPLOAD_URL + uniqueFilename;
        
        // Store mapping
        viewImageMap.put(viewId, fileUrl);
        
        log.info("Image uploaded for view {}: {}", viewId, fileUrl);
        
        return fileUrl;
    }

    public String getImageForView(Integer viewId) {
        return viewImageMap.get(viewId);
    }

    public boolean deleteImageForView(Integer viewId) {
        try {
            String imagePath = viewImageMap.get(viewId);
            if (imagePath != null) {
                boolean deleted = deleteImageFile(imagePath);
                if (deleted) {
                    viewImageMap.remove(viewId);
                    log.info("Image deleted for view: {}", viewId);
                    return true;
                }
            } else {
                log.info("No image found for view: {}", viewId);
                return true; // No image to delete, consider it successful
            }
        } catch (Exception e) {
            log.error("Error deleting image for view: {}", viewId, e);
            return false;
        }
        return false;
    }

    private boolean deleteImageFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }

            // Extract filename from URL
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path fileToDelete = Paths.get(UPLOAD_DIR + filename);

            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                log.info("File deleted successfully: {}", filename);
                return true;
            }
            
            return false;
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }

    private boolean isValidExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    // Method to initialize images for existing views (for demo purposes)
    public void initializeDefaultImages() {
        // Scan existing images and rebuild the mapping
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (Files.exists(uploadPath)) {
                Files.list(uploadPath)
                    .filter(path -> path.getFileName().toString().startsWith("view_"))
                    .forEach(path -> {
                        String filename = path.getFileName().toString();
                        // Extract viewId from filename: view_50_uuid.jpg
                        String[] parts = filename.split("_");
                        if (parts.length >= 2) {
                            try {
                                Integer viewId = Integer.parseInt(parts[1]);
                                String imageUrl = UPLOAD_URL + filename;
                                // Only keep the latest image for each view
                                if (!viewImageMap.containsKey(viewId)) {
                                    viewImageMap.put(viewId, imageUrl);
                                    log.info("Restored image mapping for view {}: {}", viewId, imageUrl);
                                } else {
                                    // If multiple images exist for same view, keep the latest one
                                    String existingImage = viewImageMap.get(viewId);
                                    if (filename.compareTo(existingImage.substring(existingImage.lastIndexOf("/") + 1)) > 0) {
                                        viewImageMap.put(viewId, imageUrl);
                                        log.info("Updated image mapping for view {}: {}", viewId, imageUrl);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Could not parse viewId from filename: {}", filename);
                            }
                        }
                    });
            }
        } catch (IOException e) {
            log.error("Error scanning existing images", e);
        }
        
        log.info("View image service initialized with {} existing images", viewImageMap.size());
    }
    
    // Method to manually refresh image mappings (useful for debugging)
    public void refreshImageMappings() {
        viewImageMap.clear();
        initializeDefaultImages();
    }
    
    // Method to get all view IDs that have images
    public java.util.Set<Integer> getViewIdsWithImages() {
        return viewImageMap.keySet();
    }
}
