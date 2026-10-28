package org.example.service.accommodation;

import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationDto create(AccommodationRequestDto accommodationRequestDto);

    Page<AccommodationDto> findAll(Pageable pageable);

    AccommodationDto getById(Long id);

    AccommodationDto update(Long id,
                            AccommodationRequestDto accommodationRequestDto);

    void deleteById(Long id);
}
