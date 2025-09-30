package hotel.service.view;

import hotel.db.entity.View;
import hotel.db.repository.view.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return viewRepository.findById(id).orElseThrow(() -> new RuntimeException("View not found"));
    }

    @Override
    public View saveView(View view) {
        return viewRepository.save(view);
    }

    @Override
    public void deleteView(Integer id) {
        viewRepository.deleteById(id);
    }

}
