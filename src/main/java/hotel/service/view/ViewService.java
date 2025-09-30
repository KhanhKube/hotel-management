package hotel.service.view;

import hotel.db.entity.View;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ViewService {
    List<View> getAllViews();
    View getViewById(Integer id);
    View saveView(View view);
    void deleteView(Integer id);
}
