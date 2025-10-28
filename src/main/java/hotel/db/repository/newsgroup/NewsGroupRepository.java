package hotel.db.repository.newsgroup;

import hotel.db.entity.NewsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsGroupRepository extends JpaRepository<NewsGroup, Integer> {

    // Find all non-deleted news groups
    List<NewsGroup> findByIsDeletedFalse();

    // Find by name and not deleted
    List<NewsGroup> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);
}
