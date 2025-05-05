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

    public Long save(Store store) {
        return storeRepository.save(store).getId();
    }

    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    public Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("가게 없음"));
    }

    public List<Store> findByCategory(String category) {
        return storeRepository.findByCategory(category);
    }
}
