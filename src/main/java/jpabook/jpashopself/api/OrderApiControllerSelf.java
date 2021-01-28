package jpabook.jpashopself.api;

import jpabook.jpashopself.domain.Order;
import jpabook.jpashopself.domain.OrderItem;
import jpabook.jpashopself.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OnetoMany 일대다 관계를 조회하고, 최적화
 * 주문내역에서 추가로 주문한 상품 정보를 추가로 조회
 */
@RestController
@RequiredArgsConstructor
public class OrderApiControllerSelf {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * Hibernate5Modul 모듈 등록, LAZY=null 처리
     * 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/find/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();

        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return all;
    }



}
