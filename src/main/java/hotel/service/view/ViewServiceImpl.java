package hotel.service.view;

import hotel.db.entity.RoomView;
import hotel.db.entity.View;
import hotel.db.repository.roomview.RoomViewRepository;
import hotel.db.repository.view.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements  ViewService{

    private final ViewRepository viewRepository;
    private final RoomViewRepository roomViewRepository;

    @Override
    public List<View> getAllViews() {
        return viewRepository.findAll().stream()
                .filter(v -> Boolean.FALSE.equals(v.getIsDeleted()))
                .toList();
    }

    @Override
    public View getViewById(Integer id) {
        View view = viewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("View not found"));
        if (Boolean.TRUE.equals(view.getIsDeleted())) {
            throw new RuntimeException("View not found");
        }
        return view;
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
        view.setIsDeleted(true);
        viewRepository.save(view);
    }

    // ROOM - VIEW (M:N)
    @Override
    public List<View> getViewsByRoomId(Integer roomId) {
        return roomViewRepository.findAll().stream()
                .filter(rv -> rv.getRoomId().equals(roomId))
                .map(RoomView::getViewId)
                .distinct()
                .map(viewId -> viewRepository.findById(viewId).orElse(null))
                .filter(v -> v != null)
                .toList();
    }

    @Override
    @Transactional
    public void addViewToRoom(Integer roomId, Integer viewId) {
        // skip if link already exists
        boolean exists = roomViewRepository.findAll().stream()
                .anyMatch(rv -> rv.getRoomId().equals(roomId) && rv.getViewId().equals(viewId));
        if (exists) {
            return;
        }
        RoomView roomView = new RoomView();
        roomView.setRoomId(roomId);
        roomView.setViewId(viewId);
        roomViewRepository.save(roomView);
    }

    @Override
    @Transactional
    public void removeViewFromRoom(Integer roomId, Integer viewId) {
        // naive remove: load all and delete matching
        roomViewRepository.findAll().stream()
                .filter(rv -> rv.getRoomId().equals(roomId) && rv.getViewId().equals(viewId))
                .forEach(roomViewRepository::delete);
    }

    @Override
    public List<Integer> getRoomIdsByViewId(Integer viewId) {
        return roomViewRepository.findAll().stream()
                .filter(rv -> rv.getViewId().equals(viewId))
                .map(RoomView::getRoomId)
                .distinct()
                .toList();
    }

}
