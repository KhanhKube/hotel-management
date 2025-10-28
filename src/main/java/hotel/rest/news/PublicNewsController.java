package hotel.rest.news;

import hotel.db.entity.News;
import hotel.service.news.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class PublicNewsController {

    private final NewsService newsService;

    @GetMapping
    public String newsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer groupId,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<News> newsPage;
        List<News> newsList;

        if (groupId != null) {
            // Get all published news from specific group
            newsList = newsService.findByNewsGroupId(groupId);
            newsList = newsList.stream()
                    .filter(news -> "PUBLISHED".equals(news.getStatus()) && news.getCreatedAt() != null)
                    .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())) // Sort by latest
                    .toList();

            // Manual pagination for filtered results
            int start = Math.min((int) pageable.getOffset(), newsList.size());
            int end = Math.min(start + pageable.getPageSize(), newsList.size());
            List<News> pageContent = newsList.subList(start, end);
            newsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, newsList.size());
        } else {
            // Use repository pagination for better performance
            newsPage = newsService.findByStatus("PUBLISHED", pageable);
        }

        model.addAttribute("newsList", newsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("totalItems", newsPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("groupId", groupId);

        return "common/news";
    }

    @GetMapping("/{id}")
    public String newsDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            News news = newsService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức với ID: " + id));

            // Only show published news to public
            if (!"PUBLISHED".equals(news.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Tin tức này chưa được xuất bản.");
                return "redirect:/news";
            }

            // Increment view count
            news.setView(news.getView() != null ? news.getView() + 1 : 1);
            newsService.saveNews(news);

            // Get related news (same group, excluding current news)
            List<News> relatedNews = null;
            if (news.getNewsGroupId() != null) {
                relatedNews = newsService.findByNewsGroupId(news.getNewsGroupId())
                        .stream()
                        .filter(n -> !n.getNewsId().equals(id)
                                && "PUBLISHED".equals(n.getStatus())
                                && n.getCreatedAt() != null) // Null safety
                        .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())) // Latest first
                        .limit(3)
                        .toList();
            }

            model.addAttribute("news", news);
            model.addAttribute("relatedNews", relatedNews);
            return "common/news-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/news";
        }
    }
}
