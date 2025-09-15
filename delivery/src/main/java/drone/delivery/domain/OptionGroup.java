package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;                 // 예: 사이즈, 매운맛, 추가 토핑

    /** 새 방식: SINGLE / MULTI */
    @Enumerated(EnumType.STRING)
    private SelectType selectType;

    /** 레거시 호환: true면 MULTI, false면 SINGLE */
    private boolean multiSelect;

    private boolean required = false;    // 필수 선택 여부
    private Integer minSelect;           // MULTI일 때 최소 선택 수 (nullable)
    private Integer maxSelect;           // MULTI일 때 최대 선택 수 (nullable)

    /** 그룹 자체의 정렬(상품 내 링크 정렬은 Link 엔티티에서 제어) */
    private Integer displayOrder = 0;

    /** 그룹에 소속된 옵션 아이템들 */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private Set<OptionItem> items = new LinkedHashSet<>();

    /* ---------- 유도/보정 로직 ---------- */

    /**
     * selectType가 null이면 multiSelect 값에서 유도(SINGLE/MULTI).
     */
    public SelectType getSelectType() {
        if (selectType != null) return selectType;
        return multiSelect ? SelectType.MULTI : SelectType.SINGLE;
    }

    /**
     * selectType을 세팅하면 multiSelect도 동기화(레거시 호환).
     */
    public void setSelectType(SelectType t) {
        this.selectType = t;
        this.multiSelect = (t == SelectType.MULTI);
    }

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (selectType == null) selectType = getSelectType(); // 멱등
        if (getSelectType() == SelectType.SINGLE) {
            // 단일 선택은 항상 0~1
            this.minSelect = 0;
            this.maxSelect = 1;
        } else {
            if (this.minSelect == null) this.minSelect = 0;
            if (this.maxSelect == null || this.maxSelect < this.minSelect) {
                this.maxSelect = this.minSelect;
            }
        }
        if (displayOrder == null) displayOrder = 0;
    }
}