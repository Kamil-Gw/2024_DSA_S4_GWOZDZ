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
     * Constructor. Assigns the final values to the properties.
     *
     * @param name
     * @param surname
     */
    public Author(String name, String surname)
    {
        this.name = name;
        this.surname = surname;
    }
}
