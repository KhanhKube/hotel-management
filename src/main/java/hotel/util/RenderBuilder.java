package hotel.util;

import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

public class RenderBuilder {
	private final String viewName;
	private final Map<String, Object> attributes = new HashMap<>();

	public RenderBuilder(final String viewName) {
		this.viewName = viewName;
	}

	public RenderBuilder with(final String key, final Object value) {
		attributes.put(key, value);
		return this;
	}

	public RenderBuilder withAll(final Map<String, Object> attrs) {
		if (attrs != null) {
			attributes.putAll(attrs);
		}
		return this;
	}

	public String build(final Model model) {
		attributes.forEach(model::addAttribute);
		return viewName;
	}
}

