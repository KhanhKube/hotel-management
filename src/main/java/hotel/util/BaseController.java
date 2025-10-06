package hotel.util;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

public abstract class BaseController {

	/* ===================== RENDER ===================== */

	// Render với 1 attribute
	protected String render(final Model model, final String viewName, final String attributeName, final Object data) {
		model.addAttribute(attributeName, data);
		return viewName;
	}

	// Render với nhiều attributes
	protected String render(final Model model, final String viewName, final Map<String, Object> attributes) {
		if (attributes != null) {
			attributes.forEach(model::addAttribute);
		}
		return viewName;
	}

	// Render đơn giản chỉ có view name
	protected String render(final String viewName) {
		return viewName;
	}

	/* ===================== REDIRECT ===================== */

	// Redirect với message (mặc định success)
	protected String redirect(final String path, final RedirectAttributes redirectAttributes, final String message) {
		return redirect(path, redirectAttributes, message, "success");
	}

	// Redirect đơn giản
	protected String redirect(final String path) {
		return "redirect:" + path;
	}

	// Redirect với message và type
	protected String redirect(final String path, final RedirectAttributes redirectAttributes,
	                          final String message, final String type) {
		if (message != null && !message.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", message);
			redirectAttributes.addFlashAttribute("messageType", type);
		}
		return "redirect:" + path;
	}

	/* ===================== BUILDER ===================== */

	protected RenderBuilder renderBuilder(final String viewName) {
		return new RenderBuilder(viewName);
	}

	protected RedirectBuilder redirectBuilder(final String path) {
		return new RedirectBuilder(path);
	}
}