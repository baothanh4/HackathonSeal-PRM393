package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.response.TeamRankingResponse;
import com.example.hackathonseal.services.Interface.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/rankings")
@RequiredArgsConstructor
@Tag(name = "Rankings", description = "Rankings and Advancement statistics")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/round/{roundId}")
    @Operation(summary = "Get ranking for a specific round. Optionally filter by categoryId")
    public ResponseEntity<List<TeamRankingResponse>> getRoundRanking(
            @PathVariable Long eventId,
            @PathVariable Long roundId,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(rankingService.getRoundRanking(eventId, roundId, categoryId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get overall ranking within a specific Category across all rounds")
    public ResponseEntity<List<TeamRankingResponse>> getCategoryRanking(
            @PathVariable Long eventId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(rankingService.getCategoryRanking(eventId, categoryId));
    }

    @GetMapping("/overall")
    @Operation(summary = "Get overall event ranking across all rounds")
    public ResponseEntity<List<TeamRankingResponse>> getEventRanking(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(rankingService.getEventRanking(eventId));
    }
}
