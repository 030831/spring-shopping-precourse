package shopping.wish;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;
import shopping.auth.JwtProvider;
import shopping.member.entity.Member;
import shopping.member.repository.MemberRepository;
import shopping.product.entity.Product;
import shopping.product.repository.ProductRepository;
import shopping.wish.entity.Wish;
import shopping.wish.repository.WishRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WishApiTest {

    static final String WISHES_URL = "/api/wishes";
    static final String OK_URL = "https://example.com/a.png";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WishRepository wishRepository;

    @Autowired
    EntityManager em;

    Member member;
    Product product;
    String token;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("wish@example.com", "hash",
                "위시"));
        product = productRepository.save(new Product("아메리카노", 4000, OK_URL));
        token = jwtProvider.createToken(member.getId());
    }

    @Test
    void 위시에_상품을_추가하면_201과_위시를_준다() {
        // when
        MvcTestResult result = addWish(token, productIdBody(product.getId()));

        // then
        assertThat(result).hasStatus(CREATED);
        assertThat(result).bodyJson().extractingPath("$.id").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.product.id")
                .isEqualTo(product.getId().intValue());
        assertThat(result).bodyJson().extractingPath("$.product.name")
                .isEqualTo("아메리카노");
        assertThat(wishRepository.existsByMemberAndProduct(member, product))
                .isTrue();
    }

    @Test
    void 같은_상품을_두_번_담으면_DUPLICATE_WISH_APPLY_이다() {
        // given
        addWish(token, productIdBody(product.getId()));

        // when
        MvcTestResult result = addWish(token, productIdBody(product.getId()));

        // then
        assertError(result, CONFLICT, "DUPLICATE_WISH_APPLY");
    }

    @Test
    void 없는_상품을_담으면_NOT_EXIST_PRODUCT이다() {
        // when
        MvcTestResult result = addWish(token, productIdBody(999_999L));

        // then
        assertError(result, NOT_FOUND, "NOT_EXIST_PRODUCT");
    }

    @Test
    void 상품_id가_없으면_PRODUCT_ID_MISSING이다() {
        // when
        MvcTestResult result = addWish(token, "{}");

        // then
        assertError(result, BAD_REQUEST, "INVALID_INPUT");
        assertThat(result).bodyJson().extractingPath("$.errors[0].code")
                .isEqualTo("PRODUCT_ID_MISSING");
    }

    @Test
    void 토큰의_회원이_없으면_INVALID_TOKEN이다() {
        // given
        String unknownMemberToken = jwtProvider.createToken(999_999L);

        // when
        MvcTestResult result = addWish(unknownMemberToken, productIdBody(product.getId()));

        // then
        assertError(result, UNAUTHORIZED, "INVALID_TOKEN");
    }

    @Test
    void 위시에_담긴_상품을_삭제하면_위시도_삭제된다() {
        // given
        Wish wish = wishRepository.save(new Wish(member, product));
        em.flush();
        em.clear();

        // when
        MvcTestResult result = mvc.delete().uri("/api/products/{productId}",
                product.getId())
                .exchange();

        em.flush();
        em.clear();

        // then
        assertThat(result).hasStatus(NO_CONTENT);
        assertThat(wishRepository.findById(wish.getId())).isEmpty();
    }

    void assertError(MvcTestResult result, HttpStatus status, String code) {
        assertThat(result).hasStatus(status);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo(code);
    }

    MvcTestResult addWish(String token, String body) {
        return mvc.post().uri(WISHES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(body)
                .exchange();
    }

    String productIdBody(Long productId) {
        return """
               {"productId": %d}
               """.formatted(productId);
    }
}
