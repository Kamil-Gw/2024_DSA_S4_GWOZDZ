package homelibrary.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookshelfController {

    @GetMapping("/bookshelf")
    public String bookshelf(){
        return "bookshelf";
    }
}
