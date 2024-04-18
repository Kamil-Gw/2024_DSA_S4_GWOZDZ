package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "book_reservation_records")
@Table(name = "book_reservation_records")
@Data
public class BookReservationRecord {
    @Id
    @SequenceGenerator(
            name = "book_reservation_records_sequence",
            sequenceName = "book_reservation_records_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "book_reservation_records_sequence"
    )
    @Column(name = "id",
            updatable = false,
            columnDefinition = "serial"
    )
    private Long id;
    @Column(
            name = "user_id",
            nullable = false,
            columnDefinition = "bigint"
    )
    private Long userId;
    @Column(
            name = "book_id",
            nullable = false,
            columnDefinition = "bigint"
    )
    private Long bookId;
    @Column(
            name = "reservation_start",
            nullable = false,
            columnDefinition = "timestamp"
    )
    private LocalDateTime reservationStart;
    @Column(
            name = "reservation_end",
            nullable = false,
            columnDefinition = "timestamp"
    )
    private LocalDateTime reservationEnd;
    @Column(
            name = "active",
            nullable = false,
            columnDefinition = "boolean"
    )
    private boolean active;

    public BookReservationRecord(
            Long userId,
            Long bookId
    ) {
        this.userId = userId;
        this.bookId = bookId;
        this.reservationStart = LocalDateTime.now();
        this.reservationEnd = this.reservationStart.plusDays(7); // TODO: make this configurable
        this.active = true;
    }
}
