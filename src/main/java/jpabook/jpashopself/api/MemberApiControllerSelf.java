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
import java.util.List;
import java.util.stream.Collectors;

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
     *
     * @RequestBody는 클라이언트가 요청한 XML/JSON 몸체를 자바 객체로 전달받을 수 있다.
     * @ResponseBody는 자바 객체를 XML/JSON으로 변환해서 응답 객체의 Body에 실어 전송할 수있음.
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

    /**
     * 회원 수정 : 회원 정보 전체를 업데이트 하는경우는 PUT방식을 사용하는것이 맞지만
     * 부분 업데이트는 POST, PATCH 방식이 더 REST 스타일에 맞다.
     * DTO를 이용해 파라미터 매핑을 해서 수정하였다.
     */
    @PostMapping("/api/update/members/{id}")
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


    /**
     * 회원 조회V1 : 응답 값으로 엔티티를 직접 외부에 노출한다.
     *             응답스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     *             엔티티가 변경되면 API 스펙이 변한다.
     *
     * @JsonIgnore을 추가하지 않아 Member <-> Order 에서 무한루프가 도는 현상이 발생한다.
     */
    @PatchMapping("/api/find/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 회원 조회V2
     */
    @GetMapping("/api/find/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName(), m.getAddress().getCity(), m.getAddress().getStreet(), m.getAddress().getZipcode()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    // Result클래스로 컬렉션을 감싸 향후 필요한 필드 추가를 용이하게 할 수 있다.
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
        private String city;
        private String street;
        private String zipcode;
    }

}
