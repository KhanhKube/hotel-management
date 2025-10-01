package hotel.db.repository.newsgroup;

import hotel.db.entity.NewsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsGroupRepository extends JpaRepository<NewsGroup, Integer> {
}
