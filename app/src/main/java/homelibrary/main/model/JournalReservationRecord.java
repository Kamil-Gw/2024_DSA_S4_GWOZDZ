package homelibrary.main.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "journal_reservation_records")
@Table(name = "journal_reservation_records")
@Data
public class JournalReservationRecord {
    @Id
    @SequenceGenerator(
            name = "journal_reservation_records_sequence",
            sequenceName = "journal_reservation_records_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = jakarta.persistence.GenerationType.SEQUENCE,
            generator = "journal_reservation_records_sequence"
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

    public JournalReservationRecord(
            Long userId,
            Long journalId
    ) {
        this.userId = userId;
        this.journalId = journalId;
        this.reservationStart = LocalDateTime.now();
        this.reservationEnd = LocalDateTime.now().plusDays(7); // TODO: make this configurable
        this.active = true;
    }
}
