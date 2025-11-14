package ru.yandex.practicum.market.core;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.market.core.config.TestcontainersConfig;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
@ImportAutoConfiguration(exclude = ReactiveOAuth2ClientAutoConfiguration.class)
public abstract class AbstractIntegrationTest {

    @MockitoBean
    private WebClient webClient;

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager clientManager;
}
