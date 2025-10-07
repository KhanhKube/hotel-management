package hotel.rest.news;

import hotel.db.entity.News;
import hotel.db.entity.NewsGroup;
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
@RequestMapping("/management/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

                    public NewsController(NewsService newsService) {
                        this.newsService = newsService;
                    }

    // F_30: Quản lý chi tiết tin (thêm, sửa, xem)
    @GetMapping
    public String newsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<News> newsPage;
        if (search != null && !search.trim().isEmpty()) {
            newsPage = newsService.searchByTitle(search, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            newsPage = newsService.findByStatus(status, pageable);
        } else {
            newsPage = newsService.findAll(pageable);
        }
        
        List<NewsGroup> newsGroups = newsService.findAllNewsGroups();
        
        model.addAttribute("newsPage", newsPage);
        model.addAttribute("newsGroups", newsGroups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("totalItems", newsPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        
        return "management/news/news-list";
    }

    @GetMapping("/add")
    public String addNewsForm(Model model) {
        List<NewsGroup> newsGroups = newsService.findAllNewsGroups();
        model.addAttribute("news", new News());
        model.addAttribute("newsGroups", newsGroups);
        return "management/news/news-form";
    }

    @PostMapping("/save")
    public String saveNews(@ModelAttribute News news, RedirectAttributes redirectAttributes) {
        try {
            newsService.saveNews(news);
            redirectAttributes.addFlashAttribute("message", 
                news.getNewsId() == null ? "Thêm tin tức thành công!" : "Cập nhật tin tức thành công!");
            return "redirect:/management/news";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/management/news/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editNewsForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            News news = newsService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức với ID: " + id));
            List<NewsGroup> newsGroups = newsService.findAllNewsGroups();
            
            model.addAttribute("news", news);
            model.addAttribute("newsGroups", newsGroups);
            return "management/news/news-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/management/news";
        }
    }

    @GetMapping("/view/{id}")
    public String viewNews(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            News news = newsService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức với ID: " + id));
            
            // Increment view count
            news.setView(news.getView() != null ? news.getView() + 1 : 1);
            newsService.saveNews(news);
            
            model.addAttribute("news", news);
            return "management/news/news-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/management/news";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteNews(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteNews(id);
            redirectAttributes.addFlashAttribute("message", "Xóa tin tức thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/management/news";
    }

    // F_31: Thay đổi trạng thái hiển thị tin
    @PostMapping("/change-status/{id}")
    public String changeNewsStatus(
            @PathVariable Integer id, 
            @RequestParam String status, 
            RedirectAttributes redirectAttributes) {
        try {
            newsService.changeNewsStatus(id, status);
            String statusText = getStatusText(status);
            redirectAttributes.addFlashAttribute("message", 
                "Thay đổi trạng thái tin tức thành " + statusText + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/management/news";
    }

    // News Groups Management
    @GetMapping("/groups")
    public String newsGroupsList(Model model) {
        List<NewsGroup> newsGroups = newsService.findAllNewsGroups();
        model.addAttribute("newsGroups", newsGroups);
        return "management/news/news-groups";
    }

    @PostMapping("/groups/save")
    public String saveNewsGroup(@ModelAttribute NewsGroup newsGroup, RedirectAttributes redirectAttributes) {
        try {
            newsService.saveNewsGroup(newsGroup);
            redirectAttributes.addFlashAttribute("message", 
                newsGroup.getNewsGroupId() == null ? "Thêm nhóm tin tức thành công!" : "Cập nhật nhóm tin tức thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/management/news/groups";
    }

    @PostMapping("/groups/delete/{id}")
    public String deleteNewsGroup(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteNewsGroup(id);
            redirectAttributes.addFlashAttribute("message", "Xóa nhóm tin tức thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/management/news/groups";
    }

    // Public news page (existing functionality)
    @GetMapping("/public")
    public String publicNewsPage(Model model) {
        List<News> publishedNews = newsService.findByStatus("PUBLISHED");
        model.addAttribute("newsList", publishedNews);
        return "news/news";
    }
    
    // Original news endpoint for backward compatibility
    @GetMapping("/news")
    public String newsPage(Model model) {
        List<News> publishedNews = newsService.findByStatus("PUBLISHED");
        model.addAttribute("newsList", publishedNews);
        return "news/news";
    }

    private String getStatusText(String status) {
        switch (status.toUpperCase()) {
            case "DRAFT": return "Bản nháp";
            case "PUBLISHED": return "Đã xuất bản";
            case "ARCHIVED": return "Đã lưu trữ";
            default: return status;
        }
    }
}