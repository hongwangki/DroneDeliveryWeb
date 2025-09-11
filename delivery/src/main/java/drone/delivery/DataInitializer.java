package drone.delivery;

import drone.delivery.domain.*;
import drone.delivery.dto.OptionGroupForm;
import drone.delivery.dto.OptionItemForm;
import drone.delivery.repository.MemberRepository;
import drone.delivery.service.OptionOwnerService;
import drone.delivery.service.StoreService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final StoreService storeService;
    private final MemberRepository memberRepository;
    private final OptionOwnerService optionOwnerService;
    @Override
    @Transactional  // 이제 정상적으로 트랜잭션 열림 + 영속성 컨텍스트 활성
    public void run(ApplicationArguments args) {
        init();   // 기존 init() 내용 그대로 호출
    }

    public void init() {

        // 1) 사장님/사용자
        final String ownerEmail = "leejd8130@naver.com";
        Member owner = memberRepository.findByEmail(ownerEmail).orElseGet(() -> {
            Member m = new Member();
            m.setName("홍왕기");
            m.setEmail(ownerEmail);
            m.setPassword("1234");
            m.setMemberType(MemberType.OWNER);
            m.setMoney(0);
            m.setAddress(new Address(
                    "경기 남양주시 오남읍 진건오남로 661",
                    "경기도 남양주시",
                    "12040",
                    "경기 남양주시 오남읍 진건오남로 661"
            ));
            return memberRepository.save(m);
        });

        final String userEmail = "1111@naver.com";
        memberRepository.findByEmail(userEmail).orElseGet(() -> {
            Member u = new Member();
            u.setName("일반사용자");
            u.setEmail(userEmail);
            u.setPassword("1234");
            u.setMemberType(MemberType.USER);
            u.setMoney(50_000);
            u.setAddress(new Address(
                    "서울특별시 종로구 세종대로 175",
                    "서울특별시",
                    "03172",
                    "정부서울청사"
            ));
            return memberRepository.save(u);
        });

        // 2) 가게 및 메뉴
        Store chickenStore = new Store();
        chickenStore.setName("맘스터치");
        chickenStore.setDescription("싸이순살 전문점");
        chickenStore.setCategory("치킨");
        chickenStore.addProduct(Product.initCreateProduct("싸이순살", 18_000,
                                100, "https://i.ibb.co/hRJqtQ7k/image.jpg"));
        chickenStore.addProduct(Product.initCreateProduct("불싸이순살", 19_000,
                                100,"https://i.ibb.co/RpY4cN9t/image.jpg"));
        chickenStore.setMinOrderPrice(25_900);
        chickenStore.setMember(owner);
        chickenStore.setImageUrl("https://i.ibb.co/fYv1WJ0V/image.png");
        storeService.save(chickenStore);

        Store koreanStore = new Store();
        koreanStore.setName("한옥집 밥상");
        koreanStore.setDescription("정갈한 한식 백반 전문");
        koreanStore.setCategory("한식");
        koreanStore.addProduct(Product.createProduct("제육볶음", 9_000, 50));
        koreanStore.addProduct(Product.createProduct("된장찌개", 8_000, 50));
        koreanStore.setMinOrderPrice(15_000);
        koreanStore.setMember(owner);
        koreanStore.setImageUrl("https://i.ibb.co/jkR8k2X4/image.png");
        storeService.save(koreanStore);

        Store chineseStore = new Store();
        chineseStore.setName("황제 중화요리");
        chineseStore.setDescription("전통 중화요리의 진수");
        chineseStore.setCategory("중식");
        chineseStore.addProduct(Product.createProduct("짜장면", 7_000, 70));
        chineseStore.addProduct(Product.createProduct("짬뽕", 8_000, 70));
        chineseStore.setMinOrderPrice(16_000);
        chineseStore.setMember(owner);
        chineseStore.setImageUrl("https://i.ibb.co/cXVhRRtT/image.png");
        storeService.save(chineseStore);

        Store pizzaStore = new Store();
        pizzaStore.setName("피자하우스");
        pizzaStore.setDescription("화덕에서 구운 수제 피자");
        pizzaStore.setCategory("피자");
        pizzaStore.addProduct(Product.createProduct("페퍼로니 피자", 15_000, 40));
        pizzaStore.addProduct(Product.createProduct("고르곤졸라 피자", 16_000, 40));
        pizzaStore.setMinOrderPrice(20_000);
        pizzaStore.setMember(owner);
        pizzaStore.setImageUrl("https://i.ibb.co/ZRwCVk4F/image.png");
        storeService.save(pizzaStore);

        Store jokbalStore = new Store();
        jokbalStore.setName("족발천하");
        jokbalStore.setDescription("야식으로 딱! 족발의 정석");
        jokbalStore.setCategory("족발");
        jokbalStore.addProduct(Product.createProduct("마늘족발", 25_000, 70));
        jokbalStore.addProduct(Product.createProduct("보쌈세트", 23_000, 60));
        jokbalStore.setMinOrderPrice(25_000);
        jokbalStore.setMember(owner);
        jokbalStore.setImageUrl("https://i.ibb.co/dJ6ZSBnh/image.jpg");
        storeService.save(jokbalStore);

        Store cafeStore = new Store();
        cafeStore.setName("커피몽");
        cafeStore.setDescription("달달한 디저트와 커피 한잔");
        cafeStore.setCategory("카페");
        cafeStore.addProduct(Product.createProduct("아메리카노", 4_000, 100));
        cafeStore.addProduct(Product.createProduct("카페라떼", 4_500, 90));
        cafeStore.setMinOrderPrice(10_000);
        cafeStore.setMember(owner);
        cafeStore.setImageUrl("https://i.ibb.co/TxWs84C5/image.png");
        storeService.save(cafeStore);

        Store burgerStore = new Store();
        burgerStore.setName("버거킹덤");
        burgerStore.setDescription("즉석에서 구운 수제버거");
        burgerStore.setCategory("햄버거");
        burgerStore.addProduct(Product.createProduct("불고기버거", 5_500, 80));
        burgerStore.addProduct(Product.createProduct("치즈버거", 6_000, 70));
        burgerStore.setMinOrderPrice(12_000);
        burgerStore.setMember(owner);
        burgerStore.setImageUrl("https://i.ibb.co/nqxZ5Qy4/image.png");
        storeService.save(burgerStore);

        Store bunsikStore = new Store();
        bunsikStore.setName("신당동 떡볶이");
        bunsikStore.setDescription("떡볶이의 원조, 분식 모음");
        bunsikStore.setCategory("분식");
        bunsikStore.addProduct(Product.createProduct("떡볶이", 5_000, 90));
        bunsikStore.addProduct(Product.createProduct("순대세트", 6_000, 85));
        bunsikStore.setMinOrderPrice(10_000);
        bunsikStore.setMember(owner);
        bunsikStore.setImageUrl("https://i.ibb.co/SXCnwh9r/image.jpg");
        storeService.save(bunsikStore);

        Store japaneseStore = new Store();
        japaneseStore.setName("스시야마");
        japaneseStore.setDescription("일본 가정식 덮밥 전문");
        japaneseStore.setCategory("일식");
        japaneseStore.addProduct(Product.createProduct("연어덮밥", 12_000, 50));
        japaneseStore.addProduct(Product.createProduct("규동", 11_000, 50));
        japaneseStore.setMinOrderPrice(20_000);
        japaneseStore.setMember(owner);
        japaneseStore.setImageUrl("https://i.ibb.co/rf7vJdq2/image.jpg");
        storeService.save(japaneseStore);

        Store dessertStore = new Store();
        dessertStore.setName("달콤한 디저트");
        dessertStore.setDescription("달콤한 하루를 위한 한 입");
        dessertStore.setCategory("디저트");
        dessertStore.addProduct(Product.createProduct("티라미수", 6_500, 40));
        dessertStore.addProduct(Product.createProduct("치즈케이크", 7_000, 35));
        dessertStore.setMinOrderPrice(8_000);
        dessertStore.setMember(owner);
        dessertStore.setImageUrl("https://i.ibb.co/0pB6WqZ4/image.jpg");
        storeService.save(dessertStore);

        // 3) 기본 옵션 자동 부착
        addChickenOptions(chickenStore, owner.getId());   // 싸이/불싸이
        addKoreanOptions(koreanStore, owner.getId());     // 제육/된장
        addChineseOptions(chineseStore, owner.getId());   // 짜장/짬뽕
        addPizzaOptions(pizzaStore, owner.getId());       // 피자
        addCafeOptions(cafeStore, owner.getId());         // 커피
        addBurgerOptions(burgerStore, owner.getId());     // 버거 커스텀
        addBunsikOptions(bunsikStore, owner.getId());     // 떡볶이
        addJapaneseOptions(japaneseStore, owner.getId()); // 덮밥
        // 디저트/족발은 기본 옵션 없음
    }

    // ---------- 옵션 생성 도우미 ----------

    private void addChickenOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        // 싸이순살
        findProduct(store, "싸이순살").ifPresent(p -> {
            Long pid = p.getId();
            ensureGroupWithItems(storeId, pid, ownerId,
                    "사이즈", true, false, 0, 1, 0,
                    new ItemSpec("스몰", 0),
                    new ItemSpec("미디움", 0),
                    new ItemSpec("라지", 500));
            ensureGroupWithItems(storeId, pid, ownerId,
                    "소스", false, true, 0, 2, 1,
                    new ItemSpec("매콤소스", 500),
                    new ItemSpec("갈릭소스", 500),
                    new ItemSpec("스윗칠리", 0));
        });
        // 불싸이순살
        findProduct(store, "불싸이순살").ifPresent(p -> {
            Long pid = p.getId();
            ensureGroupWithItems(storeId, pid, ownerId,
                    "맵기", true, false, 0, 1, 0,
                    new ItemSpec("보통", 0),
                    new ItemSpec("매움", 0),
                    new ItemSpec("아주매움", 500));
        });
    }

    private void addKoreanOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        // 제육볶음
        findProduct(store, "제육볶음").ifPresent(p -> {
            Long pid = p.getId();
            ensureGroupWithItems(storeId, pid, ownerId,
                    "공기밥 추가", false, false, 0, 1, 1,
                    new ItemSpec("공기밥 1개", 1000),
                    new ItemSpec("공기밥 2개", 2000));
            ensureGroupWithItems(storeId, pid, ownerId,
                    "곱빼기", false, false, 0, 1, 0,
                    new ItemSpec("곱빼기", 2000));
        });
        // 된장찌개
        findProduct(store, "된장찌개").ifPresent(p -> {
            Long pid = p.getId();
            ensureGroupWithItems(storeId, pid, ownerId,
                    "추가 선택", false, true, 0, 2, 0,
                    new ItemSpec("두부 추가", 500),
                    new ItemSpec("야채 추가", 500));
        });
    }

    private void addChineseOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        findProduct(store, "짜장면").ifPresent(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "곱빼기", false, false, 0, 1, 0,
                    new ItemSpec("곱빼기", 1500));
        });
        findProduct(store, "짬뽕").ifPresent(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "매운맛 선택", false, false, 0, 1, 0,
                    new ItemSpec("매운맛 업", 500));
        });
    }

    private void addPizzaOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        store.getProducts().forEach(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "크러스트", true, false, 0, 1, 0,
                    new ItemSpec("오리지널", 0),
                    new ItemSpec("치즈크러스트", 2000));
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "토핑 추가", false, true, 0, 3, 1,
                    new ItemSpec("페퍼로니 추가", 1500),
                    new ItemSpec("치즈 추가", 1500),
                    new ItemSpec("올리브 추가", 1000));
        });
    }

    private void addCafeOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        findProduct(store, "아메리카노").ifPresent(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "사이즈", true, false, 0, 1, 0,
                    new ItemSpec("톨", 0),
                    new ItemSpec("그란데", 500));
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "샷 추가", false, false, 0, 1, 1,
                    new ItemSpec("샷 1추가", 500));
        });
        findProduct(store, "카페라떼").ifPresent(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "우유 선택", false, false, 0, 1, 0,
                    new ItemSpec("일반우유", 0),
                    new ItemSpec("두유로 변경", 500));
        });
    }

    private void addBurgerOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        store.getProducts().forEach(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "세트 구성", false, false, 0, 1, 0,
                    new ItemSpec("세트 변경(감튀+콜라)", 2500));
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "추가", false, true, 0, 2, 1,
                    new ItemSpec("치즈 추가", 500),
                    new ItemSpec("패티 추가", 1500));
        });
    }

    private void addBunsikOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        findProduct(store, "떡볶이").ifPresent(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "맵기", false, false, 0, 1, 0,
                    new ItemSpec("보통", 0),
                    new ItemSpec("매움", 0));
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "사리 추가", false, true, 0, 2, 1,
                    new ItemSpec("라면사리", 1000),
                    new ItemSpec("치즈", 1000));
        });
    }

    private void addJapaneseOptions(Store store, Long ownerId) {
        Long storeId = store.getId();
        store.getProducts().forEach(p -> {
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "밥 추가", false, false, 0, 1, 1,
                    new ItemSpec("밥 곱빼기", 1000));
            ensureGroupWithItems(storeId, p.getId(), ownerId,
                    "토핑", false, true, 0, 2, 0,
                    new ItemSpec("온센타마고", 1000),
                    new ItemSpec("마요네즈", 500));
        });
    }

    private Optional<Product> findProduct(Store store, String foodName) {
        return store.getProducts().stream()
                .filter(p -> foodName.equals(p.getFoodName()))
                .findFirst();
    }

    /**
     * 그룹이 없을 때만 생성하고, 아이템을 채워 넣는다.
     */
    private void ensureGroupWithItems(Long storeId,
                                      Long productId,
                                      Long ownerId,
                                      String groupName,
                                      boolean required,
                                      boolean multiSelect,
                                      Integer minSelect,
                                      Integer maxSelect,
                                      int sortOrder,
                                      ItemSpec... items) {
        // 이미 동일 이름의 그룹이 붙어있으면 스킵
        boolean exists = optionOwnerService.getLinks(productId).stream()
                .anyMatch(l -> l.getOptionGroup() != null && groupName.equals(l.getOptionGroup().getName()));
        if (!exists) {
            // 1) 그룹 생성 + 연결
            OptionGroupForm gf = new OptionGroupForm();
            gf.setName(groupName);
            gf.setRequired(required);
            gf.setMultiSelect(multiSelect);
            gf.setMinSelect(minSelect);
            gf.setMaxSelect(maxSelect);
            optionOwnerService.createGroupAndAttach(storeId, productId, gf, sortOrder, ownerId);
        }

        // 2) 방금(또는 기존) 그룹 id 찾기
        Long groupId = optionOwnerService.getLinks(productId).stream()
                .filter(l -> l.getOptionGroup() != null && groupName.equals(l.getOptionGroup().getName()))
                .map(l -> l.getOptionGroup().getId())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("옵션그룹을 찾을 수 없습니다: " + groupName));

        // 3) 아이템들 추가 (중복 이름은 건너뜀)
        List<ProductOptionGroupLink> links = optionOwnerService.getLinks(productId);
        Set<OptionItem> existing = links.stream()
                .filter(l -> l.getOptionGroup() != null
                        && Objects.equals(l.getOptionGroup().getId(), groupId))
                .findFirst()
                .map(l -> l.getOptionGroup().getItems())   // Set<OptionItem>
                .orElseGet(Collections::emptySet);

        for (ItemSpec spec : items) {
            boolean itemExists = existing.stream().anyMatch(i -> spec.name.equals(i.getName()));
            if (itemExists) continue;

            OptionItemForm f = new OptionItemForm();
            f.setName(spec.name);
            f.setPriceDelta(spec.delta);
            // 품절로 추가하고 싶으면 stock=0, 아니면 null(제한없음)로 둠
            f.setStock(spec.soldOut ? 0 : null);
            optionOwnerService.addItem(storeId, productId, groupId, f, ownerId);
        }
    }

    // 간단한 아이템 스펙 홀더
    private record ItemSpec(String name, int delta, boolean soldOut) {
        ItemSpec(String name, int delta) { this(name, delta, false); }
    }
}