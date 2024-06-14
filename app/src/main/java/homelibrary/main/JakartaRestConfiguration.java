package homelibrary.main;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configures Jakarta RESTful Web Services for the application.
 * @author Juneau
 */
@ApplicationPath("resources")
public class JakartaRestConfiguration extends Application {
    /**
     * Default constructor.
     */
    public JakartaRestConfiguration() {
        super();
    }
    
}
