package drone.delivery.service;

import drone.delivery.GeoService;
import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import drone.delivery.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final GeoService geoService;

    /**
     * 회원가입 로직
     */
    public Long join(Member member) {
        validateDuplicateMember(member);
        validateDuplicateEmail(member);
        memberRepository.save(member);

        // 회원가입 시 주소에 따른 위도, 경도 업데이트
        try {
            geoService.updateMemberCoordinates(member);  // 주소를 기반으로 위도, 경도 업데이트
        } catch (Exception e) {
            // 위도, 경도 업데이트 실패 시 예외 처리 로직 (선택 사항)
            e.printStackTrace();
        }

        return member.getId();
    }


    public boolean validateLogin(String email, String password) {
        // 이메일로 회원을 찾고, 비밀번호가 일치하는지 확인
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.isPresent() && member.get().getPassword().equals(password);
    }

    // 이메일 중복 확인
    public boolean isEmailExist(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.isPresent(); // 이메일이 이미 존재하면 true, 없으면 false
    }

    //이름 중복확인
    public boolean isNameExist(String name) {
        // 이름을 가진 모든 멤버 리스트를 조회
        List<Member> members = memberRepository.findByName(name);

        // 리스트가 비어 있지 않으면 중복된 이름이 존재함
        return !members.isEmpty();  // 중복된 이름이 있으면 true, 없으면 false 반환
    }

    /**
     중복 검증 회원 로직
     */
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers =
                memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 이메일 중복 검증 로직
     */
    private void validateDuplicateEmail(Member member) {
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember.isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

    /**
     특정 전체 리스트로 반환하는 로직
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }


    /**
     특정 멤버 찾는 로직
     */
    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).orElse(null); // 값이 없으면 null 반환
    }

    /**
     * 회원 정보 수정 로직
     */
    @Transactional
    public void updateMember(Long memberId,String email, String name, String password, Address address, int money, Double latitude, Double longitude) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));

        // 이메일 수정
        member.setName(name);
        member.setPassword(password);
        member.setAddress(address);
        member.setMoney(money);
        member.setLatitude(latitude);
        member.setLongitude(longitude);
        member.setEmail(email);

        memberRepository.save(member);  // db에 업데이트
    }

    /**
     * 회원 탈퇴 로직
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));
        memberRepository.delete(member);
    }


    /**
     회원 이메일을 찾는 로직
     */
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }


    /**
     * 회원 Money 충전 함수
     */
    @Transactional
    public void chargeMoney(Long memberId, int amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
        member.setMoney(member.getMoney() + amount);
    }


    /**
     * 회원을 찾는 함수
     */
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다. id=" + id));
    }

}
