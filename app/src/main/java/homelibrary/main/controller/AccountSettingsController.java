package homelibrary.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountSettingsController {
    @GetMapping("/accountsettings")
    public String accountSettings() {
        return "accountSettings";
    }
}
