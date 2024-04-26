package homelibrary.main;

//import homelibrary.main.model.User;
//import homelibrary.main.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

//	@Bean
//	CommandLineRunner commandLineRunner(UserRepository userRepository) {
//		return args -> {
//			User user = new User(
//					"John",
//					"john@doe.com",
//					"password"
//			);
//			userRepository.save(user);
//		};
//	}

}
