package drone.delivery.repository;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Member,Long> {
    @EntityGraph(attributePaths = "stores")
    Optional<Member> findByIdAndMemberType(Long id, MemberType memberType);


}
