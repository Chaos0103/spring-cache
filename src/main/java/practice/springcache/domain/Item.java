package practice.springcache.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    private String itemName;
    private int price;

    private Item(String itemName, int price) {
        this.itemName = itemName;
        this.price = price;
    }

    public static Item of(String itemName, int price) {
        return new Item(itemName, price);
    }

    public Item modifyPrice(int price) {
        this.price = price;
        return this;
    }
}
