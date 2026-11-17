package shopping.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.common.exception.BusinessException;
import shopping.common.exception.ErrorCode;
import shopping.product.client.PurgoMalumClient;
import shopping.product.dto.ProductRequest;
import shopping.product.dto.ProductResponse;
import shopping.product.entity.Product;
import shopping.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PurgoMalumClient purgoMalumClient;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (purgoMalumClient.containsProfanity(request.name())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_PRODUCT_NAME);
        }

        Product product = new Product(request.name(), request.price(), request.url());
        productRepository.save(product);

        return ProductResponse.from(product);
    }
}
