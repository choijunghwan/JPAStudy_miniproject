package jpabook.jpashopself.service;

import jpabook.jpashopself.domain.item.Book;
import jpabook.jpashopself.domain.item.Item;
import jpabook.jpashopself.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    /*
     * 상품 등록
     **/
    @Transactional
    public Long join(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }
    /*
     * 상품 조회
     **/
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /*
     * 상품 수정
     **/
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
