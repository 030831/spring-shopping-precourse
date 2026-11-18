package shopping.wish.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.exception.BusinessException;
import shopping.common.exception.ErrorCode;
import shopping.member.entity.Member;
import shopping.member.repository.MemberRepository;
import shopping.product.entity.Product;
import shopping.product.repository.ProductRepository;
import shopping.wish.dto.WishRequest;
import shopping.wish.dto.WishResponse;
import shopping.wish.entity.Wish;
import shopping.wish.repository.WishRepository;

@Service
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WishResponse add(Long memberId, WishRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PRODUCT));

        if (wishRepository.existsByMemberAndProduct(member, product)) {
            throw new BusinessException(ErrorCode.DUPLICATE_WISH_APPLY);
        }

        Wish wish = wishRepository.save(new Wish(member, product));

        return WishResponse.from(wish);
    }
}
