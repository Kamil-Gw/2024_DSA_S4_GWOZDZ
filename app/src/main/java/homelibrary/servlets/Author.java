package homelibrary.servlets;

/**
 * Class representing an author.
 *
 */
public class Author
{
    /**
     * Name of the author.
     */
    public final String name;

    /**
     * Surname of the author.
     */
    public final String surname;

    /**
     * Default constructor.
     */
    Author()
    {
        name = "";
        surname = "";
    }

    /**
     * Constructor. Assigns the final values to the properties.
     *
     * @param name Name of the author.
     * @param surname Surname of the author.
     */
    public Author(String name, String surname)
    {
        this.name = name;
        this.surname = surname;
    }
}
