package jpabook.jpashopself.api;

import jpabook.jpashopself.domain.Address;
import jpabook.jpashopself.domain.Order;
import jpabook.jpashopself.domain.OrderItem;
import jpabook.jpashopself.domain.OrderStatus;
import jpabook.jpashopself.repository.OrderRepository;
import jpabook.jpashopself.repository.order.query.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * OnetoMany 일대다 관계를 조회하고, 최적화
 * 주문내역에서 추가로 주문한 상품 정보를 추가로 조회
 */
@RestController
@RequiredArgsConstructor
public class OrderApiControllerSelf {

    private final OrderRepository orderRepository;
    private final OrderQueryRepositorySelf orderQueryRepositorySelf;
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

    /**
     * v2. 엔티티를 DTO로 변환해서 출력
     * 지연 로딩으로 너무 많은 SQL이 실행된다
     * SQL실행 수  order 1번,  member, address N번, orderItem N번, item N번 발생한다.
     * 현재는 batch_fetch_size 100이 설정되어 조금의 최적화가 되어있다.
     */
    @GetMapping("/api/find/v2/orders")
    public List<OrderDtoSelf> orderV2() {
        List<Order> orders = orderRepository.findAll();

        List<OrderDtoSelf> result = orders.stream().map(order -> new OrderDtoSelf(order))
                .collect(toList());

        return result;
    }

    @Data
    static class OrderDtoSelf {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;  //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDtoSelf> orderItems;

        public OrderDtoSelf(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDtoSelf(orderItem))
                    .collect(toList());
        }

    }

    @Data
    static class OrderItemDtoSelf {

        private String itemName; //상품 명
        private int orderPrice;  //주문 가격
        private int count;       //주문 수량

        public OrderItemDtoSelf(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /**
     * v3. 엔티티를 DTO로 변환해서 출력 최적화
     * fetch join을 사용해서 SQL이 1번만 실행되게 최적화 하였다.
     * distinct를 사용한 이유는 1대다 조인이 있으므로 DB row가 증가한다.
     * 컬렉션 fetch join에서 불필요한 중복조회를 방지한다.
     * 단점 : 페이징이 불가능하다.
     */
    @GetMapping("/api/find/v3/orders")
    public List<OrderDtoSelf> orderV3() {
        List<Order> orders = orderRepository.findAllWithItemSelf();

        List<OrderDtoSelf> result = orders.stream().map(o -> new OrderDtoSelf(o))
                .collect(toList());

        return result;
    }

    /**
     * 컬렉션을 페치 조인하면 페이징이 불가능하다.
     * 컬렉션을 페치조인하면 일대다 조인이 발생해 데이터가 예측할 수 없이 증가한다.
     * 페이징은 일대다에서 일(1)을 기준으로 페이징하고 싶은데 데이터가 다(N)를 기준으로 생성되어
     * 페이징 기준이 어긋나 버린다.
     *
     * 이러한 한계를 돌파하기 위해
     * ToOne 관게는 모두 페치조인하고, 컬렉션은 지연로딩으로 조회한다.
     * 그리고 컬렉션을 지연로딩으로 조회할때 성능 최적화를 위해서 batch_fetch_size를 설정해줘서
     * 한번에 IN쿼리로 조회하도록 성능 최적화를 한다.
     */

    /**
     * v3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - ToONE 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size or @BatchSize로 최적환
     */
    @GetMapping("/api/find/v3.1/orders")
    public List<OrderDtoSelf> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                           @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDeliverySelf(offset, limit);
        List<OrderDtoSelf> result = orders.stream().map(o -> new OrderDtoSelf(o))
                .collect(toList());

        return result;
    }

    /**
     * v4. JPA에서 DTO 직접 조회
     */
    @GetMapping("/api/find/v4/orders")
    public List<OrderQueryDtoSelf> ordersV4() {
        return orderQueryRepositorySelf.findOrderQueryDtos();
    }


    /**
     * v5. JPA에서 DTO 직접 조회 - 컬렉션 조회 최적환
     */
    @GetMapping("/api/find/v5/orders")
    public List<OrderQueryDtoSelf> ordersV5() {
        return orderQueryRepositorySelf.findAllByDto_optimization();
    }

    /**
     * v6. JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDtoSelf> ordersV6() {
        List<OrderFlatDtoSelf> flats = orderQueryRepositorySelf.findALlByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDtoSelf(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDtoSelf(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDtoSelf(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress()))
                .collect(toList());
    }
}
