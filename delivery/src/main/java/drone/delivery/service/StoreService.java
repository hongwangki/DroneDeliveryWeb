package drone.delivery.service;

import drone.delivery.domain.Store;
import drone.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    //가게 등록 함수
    public Long save(Store store) {
        return storeRepository.save(store).getId();
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
}
