package homelibrary.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorController
{
    @GetMapping("/author")
    public String author()
    {
        return "author";
    }
}
