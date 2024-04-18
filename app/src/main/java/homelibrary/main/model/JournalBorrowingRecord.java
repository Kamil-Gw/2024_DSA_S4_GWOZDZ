package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "journal_borrowing_records")
@Table(name = "journal_borrowing_records")
@Data
public class JournalBorrowingRecord {
    @Id
    @SequenceGenerator(
            name = "journal_borrowing_records_sequence",
            sequenceName = "journal_borrowing_records_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "journal_borrowing_records_sequence"
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
            name = "journal_id",
            nullable = false,
            columnDefinition = "bigint"
    )
    private Long journalId;
    @Column(
            name = "borrowed",
            nullable = false,
            columnDefinition = "timestamp"
    )
    private LocalDateTime borrowed;
    @Column(
            name = "returned",
            columnDefinition = "timestamp"
    )
    private LocalDateTime returned;
    @Column(
            name = "expected_return",
            nullable = false,
            columnDefinition = "timestamp"
    )
    private LocalDateTime expectedReturn;

    public JournalBorrowingRecord(
            Long userId,
            Long journalId
    ) {
        this.userId = userId;
        this.journalId = journalId;
        this.borrowed = LocalDateTime.now();
        this.expectedReturn = this.borrowed.plusDays(14); // TODO: make this a configurable value
    }
}
