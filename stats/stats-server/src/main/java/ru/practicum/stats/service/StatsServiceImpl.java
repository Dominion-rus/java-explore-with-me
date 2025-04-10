package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitRepository repository;

    @Override
    public void saveHit(EndpointHitDto dto) {
        Hit hit = Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();

        repository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        boolean noUriFilter = (uris == null || uris.isEmpty());

        if (unique) {
            return noUriFilter
                    ? repository.findUniqueHits(start, end)
                    : repository.findUniqueHits(start, end, uris);
        } else {
            return noUriFilter
                    ? repository.findAllHits(start, end)
                    : repository.findAllHits(start, end, uris);
        }
    }
}
