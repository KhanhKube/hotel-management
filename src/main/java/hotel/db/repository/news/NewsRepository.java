package hotel.db.repository.news;
import hotel.db.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {

    List<News> findByStatusAndIsDeletedFalse(String status);

    Page<News> findByStatusAndIsDeletedFalse(String status, Pageable pageable);

    List<News> findByNewsGroupIdAndIsDeletedFalse(Integer newsGroupId);

    List<News> findByUserIdAndIsDeletedFalse(Integer userId);

    List<News> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    Page<News> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);
}
