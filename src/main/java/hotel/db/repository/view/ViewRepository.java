package hotel.db.repository.view;

import hotel.db.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewRepository extends JpaRepository<View, Integer> {
    boolean existsByViewTypeIgnoreCase(String viewType);
    List<View> findByViewTypeIgnoreCaseAndViewIdNot(String viewType, Integer viewId);
}
