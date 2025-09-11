package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_option_group_link",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_group",
                columnNames = {"product_id", "option_group_id"}
        )
)
@Getter
@Setter
public class ProductOptionGroupLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 상품과 연결되는가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 어떤 옵션 그룹인가(재사용 가능) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id")
    private OptionGroup optionGroup;

    /** 상품별 노출 제어 */
    private boolean enabled = true;

    /** 새 정렬 필드(우선) */
    private Integer displayOrder;

    /** 레거시 정렬 필드 */
    private Integer sortOrder;

    /* displayOrder 우선, 없으면 sortOrder, 둘 다 없으면 0 */
    public Integer getDisplayOrder() {
        if (displayOrder != null) return displayOrder;
        return sortOrder != null ? sortOrder : 0;
    }
    public void setDisplayOrder(Integer v) {
        this.displayOrder = v;
        this.sortOrder = v; // 레거시 동기화
    }

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (displayOrder == null && sortOrder == null) {
            displayOrder = 0;
            sortOrder = 0;
        } else if (displayOrder == null) {
            displayOrder = sortOrder;
        } else if (sortOrder == null) {
            sortOrder = displayOrder;
        }
    }
}