package homelibrary.main.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;

/**
 *
 * @author Kay Jay O'Nail
 */
@Controller
public class LoginController
{
    @GetMapping("/login")
    public String loginGet() {
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam("loginName") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        String dbUrl = "jdbc:postgresql://dsa-sec04.postgres.database.azure.com:5432/home_library";
        String dbUsername = "dsa";;
        String dbPassword = "b^&!78ieyvcKKzAce4tkNZi4qcrivKsA9@9W4KomsS$$B5yEmoCx#XpJmWSZn2msRxbo^B#toja*rWipEFDhquDp8rTvtbw7dxMie4%9gYqKTzr%&ht$hB$RXSA4p*&&";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String query = "SELECT * FROM app.users WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    session.setAttribute("username", username);
                    return "home";
                } else {
                    return "login";
                }
            }
        } catch (SQLException e) {
            return "login";
        }
    }
}

