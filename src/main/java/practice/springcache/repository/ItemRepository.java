package practice.springcache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.springcache.domain.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
