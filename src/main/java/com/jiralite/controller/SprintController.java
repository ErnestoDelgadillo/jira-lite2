package com.jiralite.controller;

import com.jiralite.dto.SprintCreateRequest;
import com.jiralite.dto.SprintResponse;
import com.jiralite.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@Valid @RequestBody SprintCreateRequest request) {
        SprintResponse response = sprintService.createSprint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintResponse> getSprint(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sprintService.getSprint(id));
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> listSprints(@RequestParam(value = "projectId", required = false) Long projectId) {
        return ResponseEntity.ok(sprintService.listSprints(projectId));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SprintResponse> startSprint(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sprintService.startSprint(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SprintResponse> completeSprint(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sprintService.completeSprint(id));
    }
}
