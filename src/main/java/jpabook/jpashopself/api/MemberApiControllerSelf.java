package jpabook.jpashopself.api;

import jpabook.jpashopself.domain.Address;
import jpabook.jpashopself.domain.Member;
import jpabook.jpashopself.repository.MemberRepository;
import jpabook.jpashopself.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

// controller에서 Data를 반환할때 ResponseBody를 이용해 Json 형식으로 데이터를 반환해야한다.
// RestController는 Controller + ResponseBody로 Json형태로 객체 데이터 반환까지 가능하다.
@RestController
@RequiredArgsConstructor
public class MemberApiControllerSelf {

    private final MemberService memberService;

    /**
     * 회원 등록V1 : 요청 값으로 Member 엔티티를 직접 받는다
     * 단점: 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * 엔티티에 API 검증을 위한 로직이 들어간다.
     * 엔티티가 변경되면 API 스펙이 변한다.
     */
    @PostMapping("/api/save/v1/members")
    public CreateMemberResponseSelf saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponseSelf(id);
    }

    @Data
    static class CreateMemberResponseSelf {
        private Long id;

        public CreateMemberResponseSelf(Long id) {
            this.id = id;
        }
    }

    /**
     * 회원등록V2 : 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     * @RequestBody는 HTTP 요청 몸체를 자바 객체로 전달받을 수 있다.
     */
    @PostMapping("/api/save/v2/members")
    public CreateMemberResponseSelf saveMemberV2(@RequestBody @Valid CreateMemberRequestSelf request) {
        Member member = new Member();
        member.setName(request.getName());

        Address address = new Address(request.getCity(), request.getStreet(), request.getZipcode());
        member.setAddress(address);

        Long id = memberService.join(member);
        return new CreateMemberResponseSelf(id);
    }

    @Data
    static class CreateMemberRequestSelf {
        private String name;
        private String city;
        private String street;
        private String zipcode;
    }

    @PutMapping("/api/update/members/{id}")
    public UpdateMemberResponseSelf updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequestSelf request) {
        memberService.updateAll(id, request.getName(), request.getCity(), request.getStreet(), request.getZipcode());

        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponseSelf(findMember.getId(), findMember.getName(), findMember.getAddress().getCity(),
                findMember.getAddress().getStreet(), findMember.getAddress().getZipcode());
    }

    @Data
    static class UpdateMemberRequestSelf {
        private String name;
        private String city;
        private String street;
        private String zipcode;
    }

    @Data
    @AllArgsConstructor
    class UpdateMemberResponseSelf {
        private Long id;
        private String name;
        private String city;
        private String street;
        private String zipcode;
    }


}
