package ru.yandex.practicum.market.core;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.market.core.config.TestcontainersConfig;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
public abstract class AbstractIntegrationTest {
}
