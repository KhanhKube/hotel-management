package hotel.service.view;

import hotel.db.entity.View;
import hotel.db.repository.view.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements  ViewService{

    private final ViewRepository viewRepository;

    @Override
    public List<View> getAllViews() {
        return viewRepository.findAll();
    }

    @Override
    public View getViewById(Integer id) {
        return viewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("View not found"));
    }

    @Override
    @Transactional
    public View saveView(View view) {
        // Enforce DB constraint: view_type NOT NULL and length <= 100
        if (view.getViewType() == null || view.getViewType().trim().isEmpty()) {
            throw new IllegalArgumentException("viewType is required");
        }
        String trimmed = view.getViewType().trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("viewType must be <= 100 characters");
        }
        view.setViewType(trimmed);
        return viewRepository.save(view);
    }

    @Override
    @Transactional
    public void deleteView(Integer id) {
        View view = viewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("View not found"));
        // Hard delete - xóa hẳn khỏi database
        viewRepository.delete(view);
    }


}
