package homelibrary.main.repository;

import homelibrary.main.model.BookReservationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReservationRecordRepository extends JpaRepository<BookReservationRecord, Long> {

}
