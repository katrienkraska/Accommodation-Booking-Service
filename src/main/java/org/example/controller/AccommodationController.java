package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.service.accommodation.AccommodationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation Controller", description = "Managing accommodation inventory")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a new accommodation unit",
            description = "Allows administrators to add a new accommodation "
                    + "with detailed information.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AccommodationDto create(@RequestBody
                                   @Valid AccommodationRequestDto accommodationRequestDto) {
        return accommodationService.create(accommodationRequestDto);
    }

    @GetMapping
    @Operation(
            summary = "Retrieve available accommodations",
            description = "Allows users to retrieve a paginated list of all active accommodations "
                    + "available for booking, including details such as type, location, pricing, "
                    + "and amenities. "
                    + "This endpoint is publicly accessible without authentication.")
    public Page<AccommodationDto> getAll(Pageable pageable) {
        return accommodationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accommodation by ID",
            description = "Allows users to retrieve detailed information about a specific "
                    + "accommodation by its unique identifier. "
                    + "This endpoint is publicly accessible without authentication.")
    public AccommodationDto getById(@PathVariable Long id) {
        return accommodationService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Update accommodation by ID",
            description = "Allows administrators to update the details "
                    + "of an existing accommodation by its unique identifier. ")
    public AccommodationDto update(@PathVariable Long id,
                                   @RequestBody @Valid AccommodationRequestDto accommodationRequestDto) {
        return accommodationService.update(id, accommodationRequestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete accommodation by ID",
            description = "Allows administrators to logically delete an existing "
                    + "accommodation by its unique identifier. ")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accommodationService.deleteById(id);
    }
}
