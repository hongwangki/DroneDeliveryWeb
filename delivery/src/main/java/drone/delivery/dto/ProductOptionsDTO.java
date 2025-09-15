package drone.delivery.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductOptionsDTO {
    private Long productId;
    private String name;
    private int basePrice;
    private List<GroupDTO> groups;
    private String imageUrl;
    private String productDescription;
    // 템플릿 호환용 게터 추가
    public String getProductName() {
        return name;              // 또는 return foodName;
    }

    @Data @Builder
    public static class GroupDTO {
        private Long groupId;
        private String name;
        private String selectType; // SINGLE / MULTI
        private boolean required;
        private Integer minSelect;
        private Integer maxSelect;
        private Integer displayOrder;
        private List<ItemDTO> items;
    }

    @Data @Builder
    public static class ItemDTO {
        private Long itemId;
        private String name;
        private int priceDelta;
        private boolean isDefault;
        private Integer stock;
        private Integer displayOrder;
    }
}