package practice.springcache.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import practice.springcache.controller.request.ItemModifyRequest;
import practice.springcache.domain.Item;
import practice.springcache.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    public List<Item> searchItems() {
        log.info("ItemController#searchItems 호출");
        return itemService.searchItems();
    }

    @GetMapping("/items/{itemId}")
    public Item searchItem(@PathVariable Long itemId) {
        log.info("ItemController#searchItem 호출 [itemId = {}]", itemId);
        return itemService.searchItem(itemId);
    }

    @PatchMapping("/items/{itemId}")
    public Item modifyItem(@PathVariable Long itemId, @RequestBody ItemModifyRequest request) {
        log.info("ItemController#modifyItem 호출 [itemId = {}, price = {}]", itemId, request.getPrice());
        return itemService.modifyItem(itemId, request.getPrice());
    }
}
