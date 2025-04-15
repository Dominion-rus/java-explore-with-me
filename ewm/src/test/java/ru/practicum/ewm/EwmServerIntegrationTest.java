package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EwmServerApplication.class)
@RequiredArgsConstructor
@Disabled
public class EwmServerIntegrationTest {

    @Autowired
    private StatsClient statsClient;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context.getBean(StatsClient.class)).isNotNull();
    }

    @Test
    void statsClient_shouldSaveAndReturnStats() {
        String testUri = "/test/ewm";
        String testApp = "ewm-service";
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(testApp)
                .uri(testUri)
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);

        List<ViewStatsDto> stats = statsClient.getStats(
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(5),
                List.of(testUri),
                false
        );

        assertThat(stats).isNotEmpty();
        assertThat(stats.get(0).getApp()).isEqualTo(testApp);
        assertThat(stats.get(0).getUri()).isEqualTo(testUri);
        assertThat(stats.get(0).getHits()).isGreaterThan(0);
    }
}
