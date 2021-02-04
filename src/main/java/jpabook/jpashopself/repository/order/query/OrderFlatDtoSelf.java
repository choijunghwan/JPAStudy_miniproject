package jpabook.jpashopself.repository.order.query;

import jpabook.jpashopself.domain.Address;
import jpabook.jpashopself.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFlatDtoSelf {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private Address address;
    private OrderStatus orderStatus;

    private String itemName;
    private int orderPrice;
    private int count;

    public OrderFlatDtoSelf(Long orderId, String name, LocalDateTime orderDate, Address address, OrderStatus orderStatus, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.address = address;
        this.orderStatus = orderStatus;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
