package ru.yandex.practicum.market.core;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.market.core.config.TestcontainersConfig;

@SpringBootTest
@Import(TestcontainersConfig.class)
public abstract class AbstractIntegrationTest {
}
