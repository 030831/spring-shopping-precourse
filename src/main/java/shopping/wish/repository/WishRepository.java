package shopping.wish.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shopping.member.entity.Member;
import shopping.product.entity.Product;
import shopping.wish.entity.Wish;

import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {

    boolean existsByMemberAndProduct(Member member, Product product);

    Optional<Wish> findByIdAndMember(Long id, Member member);
}
