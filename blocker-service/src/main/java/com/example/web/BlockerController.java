package com.example.web;

import com.example.dto.BlockerDtos.*;
import com.example.service.BlockerServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class BlockerController {
    private final BlockerServiceImpl service;

    @PostMapping("/check")
    public CheckResponse check(@Valid @RequestBody CheckRequest req) {
        return service.check(req);
    }
}
