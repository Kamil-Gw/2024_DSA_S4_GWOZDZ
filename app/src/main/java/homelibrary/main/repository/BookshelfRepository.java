package homelibrary.main.repository;

import homelibrary.main.model.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {

}
