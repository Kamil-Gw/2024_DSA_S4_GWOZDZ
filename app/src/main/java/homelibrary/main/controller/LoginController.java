package homelibrary.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author Kay Jay O'Nail
 */
@Controller
public class LoginController
{
    @GetMapping("/login")
    public String login()
    {
        /* Connect with the database to verify if the data is correct. */
        // @Mikołaj
        return "home" /* - if correct; "login" otherwise */;
    }
}
