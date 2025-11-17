package ru.yandex.practicum.market.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.market.core.ApiClient;
import ru.yandex.practicum.market.core.api.PaymentApi;

@Configuration
public class WebClientConfig {

    private final String paymentUrl;

    public WebClientConfig(@Value("${payment.service.url}") String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    @Bean
    public PaymentApi paymentControllerApi(WebClient webClient) {
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentUrl);
        return new PaymentApi(apiClient);
    }

    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager oauth2ClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(oauth2ClientManager);

        filter.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                .filter(filter)
                .build();
    }
}
