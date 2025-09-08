package drone.delivery.service;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import drone.delivery.domain.Store;
import drone.delivery.repository.OwnerRepository;
import drone.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final StoreRepository storeRepository;

    //사장님 가게를 모두 찾아서 반환하는 로직
    public List<Store> findStoresByOwnerId(Long ownerId) {
        Member owner = ownerRepository
                .findByIdAndMemberType(ownerId, MemberType.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));
        return owner.getStores();
    }


}
