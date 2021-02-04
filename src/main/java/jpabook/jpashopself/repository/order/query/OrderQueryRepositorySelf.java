package jpabook.jpashopself.repository.order.query;

import jpabook.jpashopself.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositorySelf {

    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     *
     * ToOne 관계들을 먼저 조회하고 ToMany 관계는 각각 별도로 처리한다.
     * ToMany 관계는 조인하면 row수가 증가하며 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
     */
    public List<OrderQueryDtoSelf> findOrderQueryDtos() {
        //루트 조회(toOne 코드를 모두한번에 조회)
        List<OrderQueryDtoSelf> result = findOrders();

        //루프를 돌면서 컬렉션 추가
        result.forEach(o -> {
            List<OrderItemQueryDtoSelf> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;


    }

    private List<OrderQueryDtoSelf> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashopself.repository.order.query.OrderQueryDtoSelf" +
                        "(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDtoSelf.class)
                .getResultList();
    }

    private List<OrderItemQueryDtoSelf> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashopself.repository.order.query.OrderItemQueryDtoSelf" +
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDtoSelf.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * ToOne 관계들을 먼저 조회한 후
     * ToOne 관계 조회 할때 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회한후
     * Map을 사용해 매칭해 성능향상
     * 매칭은 서비스단으로 넘어와서 하고 DB와의 접촉하는 횟수는 줄어들어 성능이 향상된다.
     */
    public List<OrderQueryDtoSelf> findAllByDto_optimization() {

        //루트 조회(ToOne 코드를 모두 한번에 조회)
        List<OrderQueryDtoSelf> result = findOrders();

        //orderItem 컬렉션을 MAP 한방에 조회
        Map<Long, List<OrderItemQueryDtoSelf>> orderItemMap = findOrderItemMap(toOrderIds(result));

        //루프를 돌면서 컬렉션 추가
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return  result;
    }

    private List<Long> toOrderIds(List<OrderQueryDtoSelf> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    private Map<Long, List<OrderItemQueryDtoSelf>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDtoSelf> orderItems = em.createQuery(
                "select new jpabook.jpashopself.repository.order.query.OrderItemQueryDtoSelf" +
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDtoSelf.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDtoSelf::getOrderId));
    }

    public List<OrderFlatDtoSelf> findALlByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashopself.repository.order.query.OrderFlatDtoSelf" +
                        "(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDtoSelf.class)
                .getResultList();
    }
}
