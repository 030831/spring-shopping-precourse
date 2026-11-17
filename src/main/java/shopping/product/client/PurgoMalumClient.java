package shopping.product.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import shopping.common.exception.BusinessException;
import shopping.common.exception.ErrorCode;

@Component
public class PurgoMalumClient {

    private final RestClient restClient =
            RestClient.create("https://www.purgomalum.com");

    public boolean containsProfanity(String text) {
        try {
            String body = restClient.get()
                    .uri("/service/containsprofanity?text={text}", text)
                    .retrieve()
                    .body(String.class);

            return "true".equals(body);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.FAIL_CALL_PURGOMALUM);
        }
    }
}
