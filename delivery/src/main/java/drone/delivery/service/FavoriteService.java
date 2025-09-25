package drone.delivery.service;

import drone.delivery.domain.Favorite;
import drone.delivery.domain.Member;
import drone.delivery.domain.Store;
import drone.delivery.dto.StoreFavoriteDto;
import drone.delivery.repository.FavoriteRepository;
import drone.delivery.repository.MemberRepository;
import drone.delivery.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    /** 찜 만들기(이미 있으면 기존 id 반환) */
    public Long createFavoriteStore(Long memberId, Long storeId) {
        if (favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)) {
            return favoriteRepository.findByMemberIdAndStoreId(memberId, storeId).get().getId();
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found: " + storeId));
        return favoriteRepository.save(Favorite.create(member, store)).getId();
    }

    /** 찜 삭제 */
    public void deleteFavoriteStore(Long memberId, Long storeId) {
        favoriteRepository.deleteByMemberIdAndStoreId(memberId, storeId);
    }

    /** 토글: true=찜됨, false=해제됨 */
    public boolean toggleFavorite(Long memberId, Long storeId) {
        Optional<Favorite> opt = favoriteRepository.findByMemberIdAndStoreId(memberId, storeId);
        if (opt.isPresent()) {
            favoriteRepository.delete(opt.get());
            return false; // 해제됨
        } else {
            createFavoriteStore(memberId, storeId);
            return true; // 찜됨
        }
    }

    /**
     * 찜 리스트 보여주기
     */
    @Transactional(readOnly = true)
    public Page<StoreFavoriteDto> getFavoriteStores(Long memberId, int page, int size) {
        return favoriteRepository.findFavoriteStores(memberId, PageRequest.of(page, size));
    }


    @Transactional(readOnly = true)
    public Set<Long> getFavoriteStoreIdSetManaged(Long memberId) {
        return memberRepository.findByIdWithFavoritesAndStore(memberId)
                .map(m -> m.getFavorites().stream()
                        .map(f -> f.getStore().getId())
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    @Transactional(readOnly = true)
    public Page<Store> getFavoriteStoresAsEntities(Long memberId, int page, int size) {
        return favoriteRepository.findFavoriteStoresAsEntities(memberId, PageRequest.of(page, size));
    }


    @Transactional
    public boolean toggleFavoriteAndReturn(Long memberId, Long storeId) {
        Optional<Favorite> opt = favoriteRepository.findByMemberIdAndStoreId(memberId, storeId);
        if (opt.isPresent()) {
            favoriteRepository.delete(opt.get());
            return false; // 해제됨
        }
        Member member = memberRepository.getReferenceById(memberId);
        Store store  = storeRepository.getReferenceById(storeId);
        favoriteRepository.save(Favorite.create(member, store));
        return true; // 추가됨
    }

}
