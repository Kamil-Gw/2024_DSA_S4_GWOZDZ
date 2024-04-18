package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "bookshelves")
@Table(name = "bookshelves")
@Data
public class Bookshelf {
    @Id
    @SequenceGenerator(
            name = "bookshelves_sequence",
            sequenceName = "bookshelves_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "bookshelves_sequence"
    )
    @Column(name = "id",
            updatable = false,
            columnDefinition = "serial"
    )
    private Long id;
    @Column(
            name = "name",
            nullable = false,
            columnDefinition = "varchar(128)"
    )
    private String name;
    @Column(
            name = "description",
            columnDefinition = "text"
    )
    private String description;
    @Column(
            name = "owner_id",
            nullable = false,
            columnDefinition = "bigint"
    )
    private Long ownerId;
    @Column(
            name = "location",
            columnDefinition = "varchar(256)"
    )
    private String location;

    public Bookshelf(
            String name,
            String description,
            Long ownerId
    ) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }
}
