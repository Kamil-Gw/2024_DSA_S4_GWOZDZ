package homelibrary.main.repository;

import homelibrary.main.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {

}
