package drone.delivery.service;

import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.domain.Store;
import drone.delivery.dto.FoodDTO;
import drone.delivery.dto.StoreDTO;
import drone.delivery.dto.StoreUpdateDTO;
import drone.delivery.repository.MemberRepository;
import drone.delivery.repository.ProductRepository;
import drone.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    //가게 등록 함수 (init 데이터 용)
    public Long save(Store store) {
        return storeRepository.save(store).getId();
    }

    public Long createStore(StoreDTO dto, Long ownerId) {
        // 소유자 로드 (신뢰 가능한 서버측 식별자)
        Member owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사장님 정보를 찾을 수 없습니다."));

        // 기본 검증 예시
        if (dto.getMinOrderPrice() < 0) {
            throw new IllegalArgumentException("최소 주문 금액은 0 이상이어야 합니다.");
        }

        // 엔티티 생성
        Store store = new Store(
                dto.getName(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getImageUrl(),
                dto.getMinOrderPrice(),
                owner
        );

        storeRepository.save(store);
        return store.getId();
    }

    //가게 전체 조회 함수
    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    //가게 찾는 함수
    public Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));
    }

    //가계 카테고리를 찾는 함수
    public List<Store> findByCategory(String category) {
        return storeRepository.findByCategory(category);
    }

    //사장님 id로 Store 찾는 함수
    public Optional<Store> findStoreByIdAndOwner(Long storeId, Long ownerId) {
        return storeRepository.findByIdAndMember_Id(storeId, ownerId);
    }


    //가게에 상품 등록하는 함수
    public Long addProductToStore(Long ownerId, Long storeId, FoodDTO req) {
        // 1) 가게 존재 + 소유자 검증
        Store store = storeRepository.findByIdAndMember_Id(storeId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게가 없거나 권한이 없습니다."));

        // 2) 값 검증
        String rawName = req.getFoodName();
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("메뉴명을 입력해주세요.");
        }
        // 공백 정리(앞뒤 공백 제거 + 연속 공백을 하나로)
        String normalizedName = rawName.trim().replaceAll("\\s+", " ");

        if (req.getFoodPrice() < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        if (req.getQuantity() <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        // 3) 가게 내 중복 메뉴명 사전 차단(대소문자 무시)
        if (productRepository.existsByStore_IdAndFoodNameIgnoreCase(storeId, normalizedName)) {
            throw new IllegalArgumentException("이미 등록된 메뉴입니다: " + normalizedName);
        }

        // 4) 생성 + 연관관계
        Product product = req.toEntity();
        product.setFoodName(normalizedName); // 정리된 이름으로 저장
        store.addProduct(product);

        // 5) 저장(동시성 레이스 대비: DB 유니크 제약 위반 캐치)
        try {
            productRepository.save(product);
        } catch (DataIntegrityViolationException dup) {
            // 동시 요청이 겹쳐 유니크 제약이 터진 경우
            throw new IllegalArgumentException("이미 등록된 메뉴입니다: " + normalizedName);
        }

        return product.getId();
    }

    //가게 수정 함수
    public Long editStore(Long storeId, StoreUpdateDTO editStore) {
        // 기존 엔티티 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다. id=" + storeId));

        // 값 수정
        store.setName(editStore.getName());
        store.setDescription(editStore.getDescription());
        store.setCategory(editStore.getCategory());
        store.setImageUrl(editStore.getImageUrl());
        store.setMinOrderPrice(editStore.getMinOrderPrice());


        return store.getId();
    }
}
