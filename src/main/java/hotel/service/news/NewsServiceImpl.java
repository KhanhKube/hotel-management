package hotel.service.news;

import hotel.db.entity.News;
import hotel.db.entity.NewsGroup;
import hotel.db.repository.news.NewsRepository;
import hotel.db.repository.newsgroup.NewsGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final NewsGroupRepository newsGroupRepository;

    @Override
    public News saveNews(News news) {
        if (news.getView() == null) {
            news.setView(0);
        }
        if (news.getStatus() == null) {
            news.setStatus("DRAFT");
        }
        return newsRepository.save(news);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<News> findById(Integer newsId) {
        return newsRepository.findById(newsId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findAll() {
        return newsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByStatus(String status) {
        return newsRepository.findByStatusAndIsDeletedFalse(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<News> findByStatus(String status, Pageable pageable) {
        return newsRepository.findByStatusAndIsDeletedFalse(status, pageable);
    }

    @Override
    public void deleteNews(Integer newsId) {
        Optional<News> newsOpt = newsRepository.findById(newsId);
        if (newsOpt.isPresent()) {
            News news = newsOpt.get();
            news.setIsDeleted(true);
            newsRepository.save(news);
        }
    }

    @Override
    public News changeNewsStatus(Integer newsId, String status) {
        Optional<News> newsOpt = newsRepository.findById(newsId);
        if (newsOpt.isPresent()) {
            News news = newsOpt.get();
            news.setStatus(status);
            return newsRepository.save(news);
        }
        throw new RuntimeException("News not found with id: " + newsId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByNewsGroupId(Integer newsGroupId) {
        return newsRepository.findByNewsGroupIdAndIsDeletedFalse(newsGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByUserId(Integer userId) {
        return newsRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsGroup> findAllNewsGroups() {
        return newsGroupRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NewsGroup> findNewsGroupById(Integer newsGroupId) {
        return newsGroupRepository.findById(newsGroupId);
    }

    @Override
    public NewsGroup saveNewsGroup(NewsGroup newsGroup) {
        return newsGroupRepository.save(newsGroup);
    }

    @Override
    public void deleteNewsGroup(Integer newsGroupId) {
        Optional<NewsGroup> newsGroupOpt = newsGroupRepository.findById(newsGroupId);
        if (newsGroupOpt.isPresent()) {
            NewsGroup newsGroup = newsGroupOpt.get();
            newsGroup.setIsDeleted(true);
            newsGroupRepository.save(newsGroup);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> searchByTitle(String title) {
        return newsRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<News> searchByTitle(String title, Pageable pageable) {
        return newsRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title, pageable);
    }
}
