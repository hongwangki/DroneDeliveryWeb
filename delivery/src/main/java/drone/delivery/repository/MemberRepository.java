package drone.delivery.repository;

import drone.delivery.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name);

    Optional<Member> findByEmail(String email);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);


    @Query("""
        select distinct m
        from Member m
        left join fetch m.favorites f
        left join fetch f.store s
        where m.id = :memberId
    """)
    Optional<Member> findByIdWithFavoritesAndStore(@Param("memberId") Long memberId);

}

