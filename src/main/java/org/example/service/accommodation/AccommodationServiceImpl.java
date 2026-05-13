package org.example.service.accommodation;

import lombok.RequiredArgsConstructor;
import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.exception.EntityNotFoundException;
import org.example.mapper.AccommodationMapper;
import org.example.mapper.AddressMapper;
import org.example.model.accommodation.Address;
import org.example.model.accommodation.Accommodation;
import org.example.model.accommodation.AccommodationType;
import org.example.model.accommodation.AmenityType;
import org.example.repository.AccommodationRepository;
import org.example.repository.AccommodationTypeRepository;
import org.example.repository.AddressRepository;
import org.example.repository.AmenityTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationTypeRepository accommodationTypeRepository;
    private final AmenityTypeRepository amenityTypeRepository;
    private final AccommodationMapper accommodationMapper;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    @Override
    public AccommodationDto create(AccommodationRequestDto accommodationRequestDto) {

        Accommodation accommodation = new Accommodation();

        AccommodationType accommodationType = accommodationTypeRepository
                .findById(accommodationRequestDto.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation type not found"));

        accommodation.setType(accommodationType);

        Address address = addressMapper.toEntity(accommodationRequestDto.getAddressDto());
        accommodation.setAddress(address);

        accommodation.setSizeType(accommodationRequestDto.getSizeType());
        accommodation.setDailyRate(accommodationRequestDto.getDailyRate());
        accommodation.setAvailability(accommodationRequestDto.getAvailability());

        Set<AmenityType> amenityTypes = accommodationRequestDto.getAmenityTypeIds()
                .stream()
                .map(id -> amenityTypeRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Amenity not found: " + id)))
                .collect(Collectors.toSet());

        accommodation.setAmenities(amenityTypes);

        Accommodation saved = accommodationRepository.save(accommodation);

        return accommodationMapper.toDto(saved);
    }

    @Override
    public Page<AccommodationDto> findAll(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .map(accommodationMapper::toDto);
    }

    @Override
    public AccommodationDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find accommodation by id: " + id));
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public AccommodationDto update(Long id,
                                   AccommodationRequestDto accommodationRequestDto) {

        Accommodation accommodation = accommodationRepository
                .findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Cant find accommodation by id "));

        accommodationMapper.updateFromDto(accommodationRequestDto, accommodation);

        AccommodationType type = accommodationTypeRepository.findById(accommodationRequestDto.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Accommodation type not found"));
        accommodation.setType(type);

        accommodation.setSizeType(accommodationRequestDto.getSizeType());

        Set<AmenityType> amenities = new HashSet<>(amenityTypeRepository
                .findAllById(accommodationRequestDto.getAmenityTypeIds()));
        accommodation.setAmenities(amenities);

        Address address = addressMapper.toEntity(accommodationRequestDto.getAddressDto());
        address.setId(accommodation.getAddress().getId());
        addressRepository.save(address);
        accommodation.setAddress(address);

        Accommodation updated = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updated);
    }

    @Override
    public void deleteById(Long id) {

        if (!accommodationRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Accommodation not found with id: " + id);
        }
        accommodationRepository.deleteById(id);
    }
}
