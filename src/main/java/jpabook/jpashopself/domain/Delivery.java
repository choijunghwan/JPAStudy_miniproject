package jpabook.jpashopself.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;  // [READY(준비) , COMP(배송)]

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;
}
