package practice.springcache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import practice.springcache.domain.Item;
import practice.springcache.repository.ItemRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitData {

    private final ItemRepository itemRepository;

    @PostConstruct
    public void init() {
        Item item1 = Item.of("MacBook Air 13", 1590000);
        Item item2 = Item.of("MacBook Air 15", 1890000);
        Item item3 = Item.of("MacBook Pro 14", 2390000);
        Item item4 = Item.of("MacBook Pro 16", 3690000);
        itemRepository.saveAll(List.of(item1, item2, item3, item4));
        log.info("Item 더미 데이터 삽입 완료");
    }
}
