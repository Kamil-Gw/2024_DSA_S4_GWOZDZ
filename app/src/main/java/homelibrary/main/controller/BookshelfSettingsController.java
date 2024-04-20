package homelibrary.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookshelfSettingsController {
    @GetMapping("/bookshelfsettings")
    public String bookshelfSettings() {
       return "bookshelfSettings";
    }
}
