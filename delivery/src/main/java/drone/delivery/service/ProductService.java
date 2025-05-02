package drone.delivery.service;

import drone.delivery.domain.Product;
import drone.delivery.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    //상품 저장 메서드
    public Long saveProduct(Product product) {
        return productRepository.save(product).getId();
    }

    //상품 한 개 검색 메서드
    public Long findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id=" + id))
                .getId();
    }

    //상품 전체 검색 메서드
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    //상품 삭제 메서드
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id=" + id));

        productRepository.delete(product);
    }


}
