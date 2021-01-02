package jpabook.jpashopself.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepositorySelf {

    private final EntityManager em;

    public List<OrderSimpleQueryDtoSelf> findOrderDtosSelf() {
        return em.createQuery(
                "select new jpabook.jpashopself.repository.order.simplequery.OrderSimpleQueryDtoSelf(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDtoSelf.class)
                .getResultList();
    }
}
