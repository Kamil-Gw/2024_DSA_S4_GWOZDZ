package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "books")
@Table(name = "books")
@Data
public class Book {
    @Id
    @SequenceGenerator(
            name = "books_sequence",
            sequenceName = "books_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "books_sequence"
    )
    @Column(name = "id",
            updatable = false,
            columnDefinition = "serial"
    )
    private Long id;
    @Column(
            name = "isbn",
            nullable = false,
            columnDefinition = "varchar(16)"
    )
    private String isbn;
    @Column(
            name = "title",
            nullable = false,
            columnDefinition = "varchar(256)"
    )
    private String title;
    @Column(
            name = "author",
            columnDefinition = "varchar(128)"
    )
    private String author;
    @Column(
            name = "publication_time",
            nullable = false,
            columnDefinition = "date"
    )
    private String publicationDate;
    @Column(
            name = "shelf_id",
            nullable = false,
            columnDefinition = "bigint"
    )
    private Long shelfId;

    public Book(
            String isbn,
            String title,
            String author,
            String publicationDate,
            Long shelfId
    ) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationDate = publicationDate;
        this.shelfId = shelfId;
    }
}
