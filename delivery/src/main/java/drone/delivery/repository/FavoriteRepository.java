package drone.delivery.repository;

import drone.delivery.domain.Favorite;
import drone.delivery.domain.Store;
import drone.delivery.dto.StoreFavoriteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite,Long> {
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);
    Optional<Favorite> findByMemberIdAndStoreId(Long memberId, Long storeId);


    @Query(
            value = """
            select new drone.delivery.dto.StoreFavoriteDto(
                f.id,
                s.id,
                s.name
            )
            from Favorite f
            join f.store s
            where f.member.id = :memberId
            order by f.createdDate desc
        """,
            countQuery = """
            select count(f)
            from Favorite f
            where f.member.id = :memberId
        """
    )
    Page<StoreFavoriteDto> findFavoriteStores(@Param("memberId") Long memberId, Pageable pageable);


    void deleteByMemberIdAndStoreId(Long memberId, Long storeId);

    @Query(
            value = """
        select s
        from Favorite f
        join f.store s
        where f.member.id = :memberId
        order by f.createdDate desc
    """,
            countQuery = """
        select count(f)
        from Favorite f
        where f.member.id = :memberId
    """
    )
    Page<Store> findFavoriteStoresAsEntities(@Param("memberId") Long memberId, Pageable pageable);

}
