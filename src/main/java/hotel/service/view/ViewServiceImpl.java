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
        
        // Check for duplicate view type (case-insensitive) only for new views
        if (view.getViewId() == null && existsByViewType(trimmed)) {
            throw new IllegalArgumentException("Loại view '" + trimmed + "' đã tồn tại!");
        }
        
        // For existing views, check if the new name conflicts with other views
        if (view.getViewId() != null) {
            List<View> existingViews = viewRepository.findByViewTypeIgnoreCaseAndViewIdNot(trimmed, view.getViewId());
            if (!existingViews.isEmpty()) {
                throw new IllegalArgumentException("Loại view '" + trimmed + "' đã tồn tại!");
            }
        }
        
        view.setViewType(trimmed);
        return viewRepository.save(view);
    }

    @Override
    public boolean existsByViewType(String viewType) {
        return viewRepository.existsByViewTypeIgnoreCase(viewType);
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
