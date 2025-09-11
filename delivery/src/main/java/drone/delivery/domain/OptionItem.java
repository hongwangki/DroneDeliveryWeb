package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class OptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private OptionGroup group;

    private String name;                     // 예: 보통, 매운맛, 치즈 추가

    /** 증감가. Integer로 두어 null 허용 → @PrePersist에서 0 보정 */
    private Integer priceDelta;              // +1000 / -500 …

    private Integer stock;                   // 옵션 개별 재고(필요 시)

    @Column(name = "is_default")
    private boolean isDefault = false;       // SINGLE일 때 초기 선택

    /** 새 정렬 필드 (기본 사용) */
    private Integer displayOrder;

    /** 레거시 정렬 필드(남아있을 수 있음) */
    private Integer sortOrder;

    /* displayOrder 우선 사용, 없으면 sortOrder, 둘 다 없으면 0 */
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
        if (priceDelta == null) priceDelta = 0;
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