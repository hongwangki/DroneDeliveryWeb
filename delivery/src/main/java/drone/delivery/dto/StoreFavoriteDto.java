package drone.delivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreFavoriteDto {
    private Long favoriteId;
    private Long storeId;
    private String storeName;


    public StoreFavoriteDto(Long favoriteId, Long storeId, String storeName) {
        this.favoriteId = favoriteId;
        this.storeId = storeId;
        this.storeName = storeName;
    }
}
