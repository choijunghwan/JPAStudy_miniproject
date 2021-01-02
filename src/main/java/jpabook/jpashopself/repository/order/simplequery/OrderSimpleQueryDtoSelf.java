package jpabook.jpashopself.repository.order.simplequery;

import jpabook.jpashopself.domain.Address;
import jpabook.jpashopself.domain.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

/**
 * @Data -> @ToString + @EqulasAndHashCode + @Getter + @Setter + @RequiredArgsConstuctor
 * @RequiredArgsConstructor : final이나 @NonNull인 필드값만 파라미터로 받는 생성자를 만든다
 * @ToString : 출력할때 클래스명(필드1명=필드1값, 필드2명=필드2값,,,) 이런식으로 출력된다.
 * @EqulasAndHashCode : equlas 와 hashCode 메소드 자동 생성이 가능하다.
 *                      callSuper 속성을 통해 부모 클래스 필드 값들도 동일한지 체크여부를 결정 할수 있다.
 */
@Data
public class OrderSimpleQueryDtoSelf {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDtoSelf(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
