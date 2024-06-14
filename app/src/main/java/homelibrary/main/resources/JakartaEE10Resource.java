package homelibrary.main.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Jakarta EE 10 Resource
 * @since Jakarta EE 10
 */
@Path("jakartaee10")
public class JakartaEE10Resource {
    /**
     * Default constructor.
     */
    public JakartaEE10Resource(){
        super();
    }

    /**
     * Ping Jakarta EE
     * @return a response
     */
    @GET
    public Response ping(){
        return Response
                .ok("ping Jakarta EE")
                .build();
    }
}
