//package homelibrary.main.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Entity(name = "journals")
//@Table(name = "journals")
//@Data
//public class Journal {
//    @Id
//    @SequenceGenerator(
//            name = "journals_sequence",
//            sequenceName = "journals_sequence",
//            allocationSize = 1
//    )
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "journals_sequence"
//    )
//    @Column(name = "id",
//            updatable = false,
//            columnDefinition = "serial"
//    )
//    private  Long id;
//    @Column(
//            name = "issn",
//            nullable = false,
//            columnDefinition = "varchar(16)"
//    )
//    private  String issn;
//    @Column(
//            name = "title",
//            nullable = false,
//            columnDefinition = "varchar(256)"
//    )
//    private  String title;
//    @Column(
//            name = "author",
//            columnDefinition = "varchar(128)"
//    )
//    private  String author;
//    @Column(
//            name = "publication_time",
//            nullable = false,
//            columnDefinition = "date"
//    )
//    private  LocalDateTime publicationDate;
//    @Column(
//            name = "shelf_id",
//            nullable = false,
//            columnDefinition = "bigint"
//    )
//    private  Long shelfId;
//
//    public Journal(
//            String issn,
//            String title,
//            String author,
//            LocalDateTime publicationDate,
//            Long shelfId
//    ) {
//        this.issn = issn;
//        this.title = title;
//        this.author = author;
//        this.publicationDate = publicationDate;
//        this.shelfId = shelfId;
//    }
//}
