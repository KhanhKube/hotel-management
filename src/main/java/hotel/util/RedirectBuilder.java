package hotel.util;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * RedirectBuilder: hỗ trợ redirect với flash message.
 */
public class RedirectBuilder {
	private final String path;
	private String message;
	private String messageType = "success";

	public RedirectBuilder(final String path) {
		this.path = path;
	}

	public RedirectBuilder withMessage(final String message) {
		this.message = message;
		return this;
	}

	public RedirectBuilder withMessageType(final String type) {
		this.messageType = type;
		return this;
	}

	public RedirectBuilder success(final String message) {
		return withMessageType("success").withMessage(message);
	}

	public RedirectBuilder error(final String message) {
		return withMessageType("error").withMessage(message);
	}

	public RedirectBuilder warning(final String message) {
		return withMessageType("warning").withMessage(message);
	}

	public RedirectBuilder info(final String message) {
		return withMessageType("info").withMessage(message);
	}

	public String build(final RedirectAttributes redirectAttributes) {
		if (message != null && !message.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", message);
			redirectAttributes.addFlashAttribute("messageType", messageType);
		}
		return "redirect:" + path;
	}

	public String build() {
		return "redirect:" + path;
	}
}
