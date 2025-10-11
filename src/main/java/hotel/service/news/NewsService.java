package hotel.service.news;

import hotel.db.entity.News;
import hotel.db.entity.NewsGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NewsService {
    
    // F_30: Quản lý chi tiết tin (thêm, sửa, xem)
    News saveNews(News news);
    
    Optional<News> findById(Integer newsId);
    
    List<News> findAll();
    
    Page<News> findAll(Pageable pageable);
    
    List<News> findByStatus(String status);
    
    Page<News> findByStatus(String status, Pageable pageable);
    
    void deleteNews(Integer newsId);
    
    // F_31: Thay đổi trạng thái hiển thị tin
    News changeNewsStatus(Integer newsId, String status);
    
    // Additional methods for news management
    List<News> findByNewsGroupId(Integer newsGroupId);
    
    List<News> findByUserId(Integer userId);
    
    List<NewsGroup> findAllNewsGroups();
    
    Optional<NewsGroup> findNewsGroupById(Integer newsGroupId);
    
    NewsGroup saveNewsGroup(NewsGroup newsGroup);
    
    void deleteNewsGroup(Integer newsGroupId);
    
    // Search functionality
    List<News> searchByTitle(String title);
    
    Page<News> searchByTitle(String title, Pageable pageable);
}
