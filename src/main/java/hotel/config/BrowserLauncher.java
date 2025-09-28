package hotel.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncher {
	@EventListener(ApplicationReadyEvent.class)
	public void openBrowser() {
		try {
			Thread.sleep(1000);

			String url = "http://localhost:8080/hotel";
			String os = System.getProperty("os.name").toLowerCase();

			if (os.contains("win")) {
				// Windows
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else if (os.contains("mac")) {
				// MacOS
				Runtime.getRuntime().exec("open " + url);
			} else if (os.contains("nix") || os.contains("nux")) {
				// Linux
				Runtime.getRuntime().exec("xdg-open " + url);
			}

			System.out.println("Browser opened with URL: " + url);

		} catch (Exception e) {
			System.out.println("Không thể tự động mở browser: " + e.getMessage());
			System.out.println("Vui lòng mở browser và truy cập: http://localhost:8080");
		}
	}
}
