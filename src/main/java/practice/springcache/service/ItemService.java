package practice.springcache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.springcache.domain.Item;
import practice.springcache.repository.ItemRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"itemCache"})
public class ItemService {

    private final ItemRepository itemRepository;

    @Cacheable
    public List<Item> searchItems() {
        log.info("ItemService#searchItems 호출");
        return itemRepository.findAll();
    }

    @Cacheable
    public Item searchItem(Long itemId) {
        log.info("ItemService#searchItem 호출 [itemId = {}]", itemId);
        return itemRepository.findById(itemId)
            .orElseThrow(IllegalArgumentException::new);
    }

    @CacheEvict(value = "itemCache", key = "#itemId")
    public Item modifyItem(Long itemId, int price) {
        log.info("ItemService#modifyItem 호출 [itemId = {}, price = {}]", itemId, price);
        return itemRepository.findById(itemId)
            .map(item -> item.modifyPrice(price))
            .orElseThrow(IllegalArgumentException::new);
    }
}
