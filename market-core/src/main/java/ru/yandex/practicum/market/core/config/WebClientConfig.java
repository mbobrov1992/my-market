package ru.yandex.practicum.market.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.market.core.ApiClient;
import ru.yandex.practicum.market.core.api.PaymentApi;

@Configuration
public class WebClientConfig {

    private final String paymentUrl;

    public WebClientConfig(@Value("${payment.service.url}") String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    @Bean
    public PaymentApi paymentControllerApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(paymentUrl);
        return new PaymentApi(apiClient);
    }
}
