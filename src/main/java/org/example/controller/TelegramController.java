package org.example.controller;

import org.example.service.telegram.TelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Telegram management", description = "Endpoints manage telegram bot")
@RestController
@RequestMapping(value = "/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;

    @GetMapping("/invite")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Generate Telegram invitation link",
            description = "Generates a unique Telegram bot invitation "
                    + "link associated with the authenticated user.")
    public String getTelegramInviteUrl(Authentication authentication) {
        return telegramService.getTelegramInviteUrl(authentication.getName());
    }
}
