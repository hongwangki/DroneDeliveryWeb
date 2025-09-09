package drone.delivery;

import drone.delivery.domain.*;
import drone.delivery.service.MemberService;
import drone.delivery.service.StoreService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer {

    private final StoreService storeService;
    private final MemberService memberService;

    @PostConstruct
    public void init() {

        Member member= new Member();
        member.setMoney(500000);
        member.setName("강현민");
        member.setMemberType(MemberType.USER);
        member.setEmail("1");
        Address address = new Address();
        address.setCity("1");
        address.setDetailAddress("1");
        address.setStreet("1");
        address.setZipcode("1");
        member.setAddress(address);
        member.setPassword("1");
        memberService.save(member);

        // 치킨
        Store chickenStore = new Store();
        chickenStore.setName("맘스터치");
        chickenStore.setDescription("싸이순살 전문점");
        chickenStore.setCategory("치킨");
        chickenStore.addProduct(Product.createProduct("싸이순살", 18000, 100));
        chickenStore.addProduct(Product.createProduct("불싸이순살", 19000, 100));
        chickenStore.setMinOrderPrice(25900);
        storeService.save(chickenStore);

        // 한식
        Store koreanStore = new Store();
        koreanStore.setName("한옥집 밥상");
        koreanStore.setDescription("정갈한 한식 백반 전문");
        koreanStore.setCategory("한식");
        koreanStore.addProduct(Product.createProduct("제육볶음", 9000, 50));
        koreanStore.addProduct(Product.createProduct("된장찌개", 8000, 50));
        koreanStore.setMinOrderPrice(15000);
        storeService.save(koreanStore);

        // 중식
        Store chineseStore = new Store();
        chineseStore.setName("황제 중화요리");
        chineseStore.setDescription("전통 중화요리의 진수");
        chineseStore.setCategory("중식");
        chineseStore.addProduct(Product.createProduct("짜장면", 7000, 70));
        chineseStore.addProduct(Product.createProduct("짬뽕", 8000, 70));
        chineseStore.setMinOrderPrice(16000);
        storeService.save(chineseStore);

        // 피자
        Store pizzaStore = new Store();
        pizzaStore.setName("피자하우스");
        pizzaStore.setDescription("화덕에서 구운 수제 피자");
        pizzaStore.setCategory("피자");
        pizzaStore.addProduct(Product.createProduct("페퍼로니 피자", 15000, 40));
        pizzaStore.addProduct(Product.createProduct("고르곤졸라 피자", 16000, 40));
        pizzaStore.setMinOrderPrice(20000);
        storeService.save(pizzaStore);

        // 족발
        Store jokbalStore = new Store();
        jokbalStore.setName("족발천하");
        jokbalStore.setDescription("야식으로 딱! 족발의 정석");
        jokbalStore.setCategory("족발");
        jokbalStore.addProduct(Product.createProduct("마늘족발", 25000, 70));
        jokbalStore.addProduct(Product.createProduct("보쌈세트", 23000, 60));
        jokbalStore.setMinOrderPrice(25000);
        storeService.save(jokbalStore);

        // 카페
        Store cafeStore = new Store();
        cafeStore.setName("커피몽");
        cafeStore.setDescription("달달한 디저트와 커피 한잔");
        cafeStore.setCategory("카페");
        cafeStore.addProduct(Product.createProduct("아메리카노", 4000, 100));
        cafeStore.addProduct(Product.createProduct("카페라떼", 4500, 90));
        cafeStore.setMinOrderPrice(10000);
        storeService.save(cafeStore);

        // 햄버거
        Store burgerStore = new Store();
        burgerStore.setName("버거킹덤");
        burgerStore.setDescription("즉석에서 구운 수제버거");
        burgerStore.setCategory("햄버거");
        burgerStore.addProduct(Product.createProduct("불고기버거", 5500, 80));
        burgerStore.addProduct(Product.createProduct("치즈버거", 6000, 70));
        burgerStore.setMinOrderPrice(12000);
        storeService.save(burgerStore);

        // 분식
        Store bunsikStore = new Store();
        bunsikStore.setName("신당동 떡볶이");
        bunsikStore.setDescription("떡볶이의 원조, 분식 모음");
        bunsikStore.setCategory("분식");
        bunsikStore.addProduct(Product.createProduct("떡볶이", 5000, 90));
        bunsikStore.addProduct(Product.createProduct("순대세트", 6000, 85));
        bunsikStore.setMinOrderPrice(10000);
        storeService.save(bunsikStore);

        // 일식
        Store japaneseStore = new Store();
        japaneseStore.setName("스시야마");
        japaneseStore.setDescription("일본 가정식 덮밥 전문");
        japaneseStore.setCategory("일식");
        japaneseStore.addProduct(Product.createProduct("연어덮밥", 12000, 50));
        japaneseStore.addProduct(Product.createProduct("규동", 11000, 50));
        japaneseStore.setMinOrderPrice(20000);
        storeService.save(japaneseStore);

        // 디저트
        Store dessertStore = new Store();
        dessertStore.setName("달콤한 디저트");
        dessertStore.setDescription("달콤한 하루를 위한 한 입");
        dessertStore.setCategory("디저트");
        dessertStore.addProduct(Product.createProduct("티라미수", 6500, 40));
        dessertStore.addProduct(Product.createProduct("치즈케이크", 7000, 35));
        dessertStore.setMinOrderPrice(8000);
        storeService.save(dessertStore);
    }
}
