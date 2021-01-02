package jpabook.jpashopself.api;

import jpabook.jpashopself.domain.Address;
import jpabook.jpashopself.domain.Order;
import jpabook.jpashopself.domain.OrderStatus;
import jpabook.jpashopself.repository.OrderRepository;
import jpabook.jpashopself.repository.order.simplequery.OrderSimpleQueryDtoSelf;
import jpabook.jpashopself.repository.order.simplequery.OrderSimpleQueryRepositorySelf;
import jpabook.jpashopself.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * XToOne(ManyToOne, OneToOne) 관계 최적화
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiControllerSelf {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepositorySelf orderSimpleQueryRepositorySelf;

    /**
     * 주문 조회 V1: 엔티티를 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore 처리 필요
     * - Jackson 라이브러리는 기복적으로 프록시 객체를 json으로 어떻게 생성해야 하는지 몰라
     * lazy 강제 초기화를 하거나 null처리가 필요
     * - Hibernate5Module을 스프링 빈으로 등록하면 해결가능
     */
    @GetMapping("/api/find/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();

        for (Order order : all) {
            order.getMember().getName();  //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }

        return all;
    }

    /**
     * 주문 조회 V2: 엔티티를 DTO로 변환
     * - 이 방식은 1 + N번 쿼리가 실행된다.
     * - order -> Member 지연 로딩 조회 N번,  order-> Delivery 지연 로딩 조회 N번
     * - 현재는 batch-fetch-size를 default값 100을 설정해서 약간 최적화 되어 있다.
     */
    @GetMapping("/api/find/v2/simple-orders")
    public List<SimpleOrderDtoSelf> ordersV2() {
        List<Order> orders = orderRepository.findAll();

        List<SimpleOrderDtoSelf> result = orders.stream()
                .map(o -> new SimpleOrderDtoSelf(o))
                .collect(toList());

        return result;
    }

    @Data
    static class SimpleOrderDtoSelf {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDtoSelf(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

    /**
     * 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화
     * - 엔티티를 페치 조인을 사용해서 쿼리 1번에 조회
     */
    @GetMapping("/api/find/v3/simple-orders")
    public List<SimpleOrderDtoSelf> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDeliverySelf();

        List<SimpleOrderDtoSelf> result = orders.stream()
                .map(o -> new SimpleOrderDtoSelf(o))
                .collect(toList());

        return result;
    }

    /**
     * 주문 조회 V4: JPA에서 DTO로 바로 조회
     * - 쿼리 1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회 가능
     * - 테이블의 컬럼수가 엄청 많아 선택적으로 데이터를 뽑을때 사용
     * - 허나 생각보다 성능 향상은 미비하고, 리포지토리 재사용성이 떨어지며, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점이 있다.
     */
    @GetMapping("/api/find/v4/simple-orders")
    public List<OrderSimpleQueryDtoSelf> orderV4() {
        return orderSimpleQueryRepositorySelf.findOrderDtosSelf();
    }


}
