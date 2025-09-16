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

    // ✅ 모든 가게에 동일 적용할 기본 주소/좌표
    private static final Address DEFAULT_ADDR = new Address(
            "경기 남양주시 오남읍 진건오남로 661",
            "경기도 남양주시",
            "12040",
            "동부아파트"
    );
    private static final double DEFAULT_LAT = 37.6918871325482;
    private static final double DEFAULT_LNG = 127.213920335228;

    /** ✅ 공통 위치 세팅 헬퍼 */
    private void applyDefaultLocation(Store s) {
        s.setAddress(new Address(
                DEFAULT_ADDR.getStreet(),
                DEFAULT_ADDR.getCity(),
                DEFAULT_ADDR.getZipcode(),
                DEFAULT_ADDR.getDetailAddress()
        ));
        s.setLatitude(DEFAULT_LAT);
        s.setLongitude(DEFAULT_LNG);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        init();
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

        // 2) 가게 및 메뉴 + ✨상품 설명 세팅
        Store chickenStore = new Store();
        chickenStore.setName("맘스터치");
        chickenStore.setDescription("싸이순살 전문점");
        chickenStore.setCategory("치킨");

        Product p1 = Product.initCreateProduct("싸이순살", 18_000, 100,
                "https://i.ibb.co/hRJqtQ7k/image.jpg");
        p1.setProductDescription("겉바속촉 순살에 시그니처 양념을 더한 베스트 메뉴. 혼밥도, 안주도 찰떡이에요.");
        chickenStore.addProduct(p1);

        Product p2 = Product.initCreateProduct("불싸이순살", 19_000, 100,
                "https://i.ibb.co/RpY4cN9t/image.jpg");
        p2.setProductDescription("불향 가득 매콤양념으로 감칠맛을 끌어올렸어요. 달콤한 갈릭소스와도 잘 어울립니다.");
        chickenStore.addProduct(p2);

        chickenStore.setMinOrderPrice(25_900);
        chickenStore.setMember(owner);
        chickenStore.setImageUrl("https://i.ibb.co/fYv1WJ0V/image.png");
        applyDefaultLocation(chickenStore);
        storeService.save(chickenStore);

        Store koreanStore = new Store();
        koreanStore.setName("한옥집 밥상");
        koreanStore.setDescription("정갈한 한식 백반 전문");
        koreanStore.setCategory("한식");

        Product k1 = Product.createProduct("제육볶음", 9_000, 50);
        k1.setProductDescription("국내산 앞다리살을 매콤달콤 고추장 양념에 볶아낸 밥도둑 메뉴입니다.");
        koreanStore.addProduct(k1);

        Product k2 = Product.createProduct("된장찌개", 8_000, 50);
        k2.setProductDescription("자연 숙성된 된장에 채소와 두부를 듬뿍 넣어 구수하게 끓여냈어요.");
        koreanStore.addProduct(k2);

        koreanStore.setMinOrderPrice(15_000);
        koreanStore.setMember(owner);
        koreanStore.setImageUrl("https://i.ibb.co/jkR8k2X4/image.png");
        applyDefaultLocation(koreanStore);
        storeService.save(koreanStore);

        Store chineseStore = new Store();
        chineseStore.setName("황제 중화요리");
        chineseStore.setDescription("전통 중화요리의 진수");
        chineseStore.setCategory("중식");

        Product c1 = Product.createProduct("짜장면", 7_000, 70);
        c1.setProductDescription("춘장과 채소를 센불에 볶아 감칠맛을 살린 정통 자장면.");
        chineseStore.addProduct(c1);

        Product c2 = Product.createProduct("짬뽕", 8_000, 70);
        c2.setProductDescription("해물·채소를 우려낸 시원하고 얼큰한 국물의 클래식 짬뽕.");
        chineseStore.addProduct(c2);

        chineseStore.setMinOrderPrice(16_000);
        chineseStore.setMember(owner);
        chineseStore.setImageUrl("https://i.ibb.co/cXVhRRtT/image.png");
        applyDefaultLocation(chineseStore);
        storeService.save(chineseStore);

        Store pizzaStore = new Store();
        pizzaStore.setName("피자하우스");
        pizzaStore.setDescription("화덕에서 구운 수제 피자");
        pizzaStore.setCategory("피자");

        Product z1 = Product.createProduct("페퍼로니 피자", 15_000, 40);
        z1.setProductDescription("쫄깃한 도우 위에 페퍼로니를 듬뿍 올린 기본에 충실한 인기 메뉴.");
        pizzaStore.addProduct(z1);

        Product z2 = Product.createProduct("고르곤졸라 피자", 16_000, 40);
        z2.setProductDescription("고르곤졸라 치즈의 고소함에 달콤한 꿀이 어우러지는 담백한 조합.");
        pizzaStore.addProduct(z2);

        pizzaStore.setMinOrderPrice(20_000);
        pizzaStore.setMember(owner);
        pizzaStore.setImageUrl("https://i.ibb.co/ZRwCVk4F/image.png");
        applyDefaultLocation(pizzaStore);
        storeService.save(pizzaStore);

        Store jokbalStore = new Store();
        jokbalStore.setName("족발천하");
        jokbalStore.setDescription("야식으로 딱! 족발의 정석");
        jokbalStore.setCategory("족발");

        Product j1 = Product.createProduct("마늘족발", 25_000, 70);
        j1.setProductDescription("잡내 없이 삶아낸 족발에 알싸한 마늘 소스를 더해 풍미를 살렸어요.");
        jokbalStore.addProduct(j1);

        Product j2 = Product.createProduct("보쌈세트", 23_000, 60);
        j2.setProductDescription("부드러운 수육과 아삭한 김치가 한 상으로, 가족 외식에 안성맞춤.");
        jokbalStore.addProduct(j2);

        jokbalStore.setMinOrderPrice(25_000);
        jokbalStore.setMember(owner);
        jokbalStore.setImageUrl("https://i.ibb.co/dJ6ZSBnh/image.jpg");
        applyDefaultLocation(jokbalStore);
        storeService.save(jokbalStore);

        Store cafeStore = new Store();
        cafeStore.setName("커피몽");
        cafeStore.setDescription("달달한 디저트와 커피 한잔");
        cafeStore.setCategory("카페");

        Product cf1 = Product.createProduct("아메리카노", 4_000, 100);
        cf1.setProductDescription("갓 내린 원두의 균형 잡힌 산미와 바디감. 매일 마시기 좋은 기본 커피입니다.");
        cafeStore.addProduct(cf1);

        Product cf2 = Product.createProduct("카페라떼", 4_500, 90);
        cf2.setProductDescription("진한 에스프레소에 신선한 우유를 더한 부드러운 라떼.");
        cafeStore.addProduct(cf2);

        cafeStore.setMinOrderPrice(10_000);
        cafeStore.setMember(owner);
        cafeStore.setImageUrl("https://i.ibb.co/TxWs84C5/image.png");
        applyDefaultLocation(cafeStore);
        storeService.save(cafeStore);

        Store burgerStore = new Store();
        burgerStore.setName("버거킹덤");
        burgerStore.setDescription("즉석에서 구운 수제버거");
        burgerStore.setCategory("햄버거");

        Product b1 = Product.createProduct("불고기버거", 5_500, 80);
        b1.setProductDescription("달짝지근한 불고기 소스와 촉촉한 패티가 만난 한국인의 버거 클래식.");
        burgerStore.addProduct(b1);

        Product b2 = Product.createProduct("치즈버거", 6_000, 70);
        b2.setProductDescription("고소한 치즈와 패티의 정석 조합. 부담 없이 즐기는 맛.");
        burgerStore.addProduct(b2);

        burgerStore.setMinOrderPrice(12_000);
        burgerStore.setMember(owner);
        burgerStore.setImageUrl("https://i.ibb.co/nqxZ5Qy4/image.png");
        applyDefaultLocation(burgerStore);
        storeService.save(burgerStore);

        Store bunsikStore = new Store();
        bunsikStore.setName("신당동 떡볶이");
        bunsikStore.setDescription("떡볶이의 원조, 분식 모음");
        bunsikStore.setCategory("분식");

        Product bs1 = Product.createProduct("떡볶이", 5_000, 90);
        bs1.setProductDescription("쫀득한 밀떡에 달콤매콤 양념이 배인 국민 간식.");
        bunsikStore.addProduct(bs1);

        Product bs2 = Product.createProduct("순대세트", 6_000, 85);
        bs2.setProductDescription("따끈한 순대와 야들야들한 간·허파까지 푸짐하게 담았어요.");
        bunsikStore.addProduct(bs2);

        bunsikStore.setMinOrderPrice(10_000);
        bunsikStore.setMember(owner);
        bunsikStore.setImageUrl("https://i.ibb.co/SXCnwh9r/image.jpg");
        applyDefaultLocation(bunsikStore);
        storeService.save(bunsikStore);

        Store japaneseStore = new Store();
        japaneseStore.setName("스시야마");
        japaneseStore.setDescription("일본 가정식 덮밥 전문");
        japaneseStore.setCategory("일식");

        Product jp1 = Product.createProduct("연어덮밥", 12_000, 50);
        jp1.setProductDescription("신선한 연어를 두툼하게 올린 한 그릇. 와사비 간장과 찰떡궁합.");
        japaneseStore.addProduct(jp1);

        Product jp2 = Product.createProduct("규동", 11_000, 50);
        jp2.setProductDescription("달콤짭조름하게 졸인 소고기와 양파를 듬뿍 얹은 일본식 덮밥.");
        japaneseStore.addProduct(jp2);

        japaneseStore.setMinOrderPrice(20_000);
        japaneseStore.setMember(owner);
        japaneseStore.setImageUrl("https://i.ibb.co/rf7vJdq2/image.jpg");
        applyDefaultLocation(japaneseStore);
        storeService.save(japaneseStore);

        Store dessertStore = new Store();
        dessertStore.setName("달콤한 디저트");
        dessertStore.setDescription("달콤한 하루를 위한 한 입");
        dessertStore.setCategory("디저트");

        Product d1 = Product.createProduct("티라미수", 6_500, 40);
        d1.setProductDescription("마스카포네 크림에 에스프레소가 스며든 촉촉하고 진한 티라미수.");
        dessertStore.addProduct(d1);

        Product d2 = Product.createProduct("치즈케이크", 7_000, 35);
        d2.setProductDescription("꾸덕하고 진한 치즈 풍미를 담은 정성스러운 한 조각.");
        dessertStore.addProduct(d2);

        dessertStore.setMinOrderPrice(8_000);
        dessertStore.setMember(owner);
        dessertStore.setImageUrl("https://i.ibb.co/0pB6WqZ4/image.jpg");
        applyDefaultLocation(dessertStore);
        storeService.save(dessertStore);

        // 3) 기본 옵션 자동 부착
        addChickenOptions(chickenStore, owner.getId());
        addKoreanOptions(koreanStore, owner.getId());
        addChineseOptions(chineseStore, owner.getId());
        addPizzaOptions(pizzaStore, owner.getId());
        addCafeOptions(cafeStore, owner.getId());
        addBurgerOptions(burgerStore, owner.getId());
        addBunsikOptions(bunsikStore, owner.getId());
        addJapaneseOptions(japaneseStore, owner.getId());
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