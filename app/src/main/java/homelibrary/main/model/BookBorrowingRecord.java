//package homelibrary.main.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Entity(name = "book_borrowing_records")
//@Table(name = "book_borrowing_records")
//@Data
//public class BookBorrowingRecord {
//    @Id
//    @SequenceGenerator(
//            name = "book_borrowing_records_sequence",
//            sequenceName = "book_borrowing_records_sequence",
//            allocationSize = 1
//    )
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "book_borrowing_records_sequence"
//    )
//    @Column(name = "id",
//            updatable = false,
//            columnDefinition = "serial"
//    )
//    private Long id;
//    @Column(
//            name = "user_id",
//            nullable = false,
//            columnDefinition = "bigint"
//    )
//    private Long userId;
//    @Column(
//            name = "book_id",
//            nullable = false,
//            columnDefinition = "bigint"
//    )
//    private Long bookId;
//    @Column(
//            name = "borrowed",
//            nullable = false,
//            columnDefinition = "timestamp"
//    )
//    private LocalDateTime borrowed;
//    @Column(
//            name = "returned",
//            columnDefinition = "timestamp"
//    )
//    private LocalDateTime returned;
//    @Column(
//            name = "expected_return",
//            nullable = false,
//            columnDefinition = "timestamp"
//    )
//    private LocalDateTime expectedReturn;
//
//    public BookBorrowingRecord(
//            Long userId,
//            Long bookId
//    ) {
//        this.userId = userId;
//        this.bookId = bookId;
//        this.borrowed = LocalDateTime.now();
//        this.expectedReturn = LocalDateTime.now().plusDays(14); // TODO: make this a configurable value
//    }
//}
