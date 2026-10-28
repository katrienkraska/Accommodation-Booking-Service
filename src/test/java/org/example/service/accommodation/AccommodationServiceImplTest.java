package org.example.service.accommodation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.testutil.AccommodationTestUtil.getAccommodation;
import static org.example.testutil.AccommodationTestUtil.getAccommodationDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.example.dto.accommodation.AccommodationDto;
import org.example.dto.accommodation.AccommodationRequestDto;
import org.example.exception.EntityNotFoundException;
import org.example.mapper.AccommodationMapper;
import org.example.mapper.AddressMapper;
import org.example.model.accommodation.Accommodation;
import org.example.model.accommodation.AccommodationType;
import org.example.model.accommodation.Address;
import org.example.model.accommodation.AmenityType;
import org.example.repository.AccommodationRepository;
import org.example.repository.AccommodationTypeRepository;
import org.example.repository.AddressRepository;
import org.example.repository.AmenityTypeRepository;
import org.example.testutil.AccommodationTestUtil;
import org.example.testutil.AccommodationTypeTestUtil;
import org.example.testutil.AddressTestUtil;
import org.example.testutil.AmenityTypeTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private AmenityTypeRepository amenityTypeRepository;

    @Mock
    private AccommodationTypeRepository accommodationTypeRepository;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private Accommodation accommodation;
    private AccommodationDto accommodationDto;
    private AccommodationType accommodationType;
    private Address address;
    private AmenityType amenityType;
    private AccommodationRequestDto accommodationRequestDto;

    @BeforeEach
    void setUp() {
        accommodation = getAccommodation();
        accommodationRequestDto = AccommodationTestUtil.createSampleAccommodationRequestDto();
        accommodationDto = getAccommodationDto();
        accommodationType = AccommodationTypeTestUtil.getAccommodationType();
        address = AddressTestUtil.getAddress();
        amenityType = AmenityTypeTestUtil.getAmenityType();
    }

    @Test
    @DisplayName("Сreate accommodation with valid values")
    void create_ValidData_ShouldReturnAccommodationDto() {
        accommodationRequestDto.getAmenityTypeIds().forEach(amenityId ->
                when(amenityTypeRepository.findById(amenityId))
                        .thenReturn(Optional.of(amenityType)));

        AccommodationDto expected = accommodationDto;

        when(accommodationTypeRepository.findById(accommodationRequestDto.getTypeId()))
                .thenReturn(Optional.of(accommodation.getType()));

        when(addressMapper.toEntity(accommodationRequestDto.getAddressDto()))
                .thenReturn(accommodation.getAddress());

        when(accommodationRepository.save(any(Accommodation.class)))
                .thenReturn(accommodation);

        when(accommodationMapper.toDto(any(Accommodation.class)))
                .thenReturn(accommodationDto);

        AccommodationDto actual =
                accommodationService.create(accommodationRequestDto);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Save accommodation with invalid values")
    void save_NotValidData_ShouldReturnException() {
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class, () -> accommodationService
                        .create(accommodationRequestDto));

        String expectedMessage = "Accommodation type not found";

        assertEquals(expectedMessage, entityNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Get accommodation by id")
    void getById_ExistingAccommodation_ShouldReturnAccommodationDto() {
        final AccommodationDto expected = accommodationDto;

        Long id = 1L;

        when(accommodationRepository.findById(id)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationDto);

        AccommodationDto actualAccommodationById = accommodationService.getById(id);

        assertNotNull(actualAccommodationById);
        assertThat(actualAccommodationById).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Save accommodation with invalid values")
    void getById_NotExistingAccommodation_ShouldReturnException() {
        Long id = 999L;

        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class, () -> accommodationService.getById(id));

        String expectedMessage = "Can't find accommodation by id: " + id;

        assertEquals(expectedMessage, entityNotFoundException.getMessage());
    }

    @Test
    @DisplayName("Find all existing accommodations")
    void findAll_ExistingAccommodation_ShouldReturnListAccommodationDto() {
        List<Accommodation> accommodationList = List.of(accommodation);
        Pageable pageable = PageRequest.of(1, 1);
        Page<Accommodation> accommodationPage = new PageImpl<>(accommodationList);

        when(accommodationMapper.toDto(accommodation)).thenReturn(accommodationDto);
        when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);

        Page<AccommodationDto> actual = accommodationService.findAll(pageable);
        assertEquals(1, actual.getTotalElements());

        AccommodationDto actualAccommodation = actual.getContent().get(0);

        assertThat(actualAccommodation).usingRecursiveComparison()
                .isEqualTo(accommodationDto);
    }

    @Test
    @DisplayName("Update existing accommodation and return accommodation dto")
    void update_ExistingAccommodation_ShouldReturnAccommodationDto() {
        Long id = 1L;

        AccommodationDto expected = accommodationDto;

        when(accommodationRepository.findById(id)).thenReturn(Optional.of(accommodation));
        when(accommodationTypeRepository.findById(accommodationRequestDto.getTypeId()))
                .thenReturn(Optional.of(accommodationType));
        when(addressMapper.toEntity(accommodationRequestDto.getAddressDto())).thenReturn(address);
        when(addressRepository.save(address)).thenReturn(address);
        when(accommodationMapper.toDto(any(Accommodation.class))).thenReturn(accommodationDto);
        when(amenityTypeRepository.findAllById(accommodationRequestDto.getAmenityTypeIds()))
                .thenReturn(List.of(amenityType));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);

        AccommodationDto actualUpdate = accommodationService.update(id, accommodationRequestDto);

        assertThat(actualUpdate).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Update a non-existing accommodation")
    void update_NonExistingAccommodation_ShouldReturnException() {
        Long id = 1L;

        EntityNotFoundException actualMessage = assertThrows(
                EntityNotFoundException.class, () -> accommodationService.update(id, accommodationRequestDto));

        String expectedMessage = "Cant find accommodation by id";

        assertEquals(expectedMessage, actualMessage.getMessage());
    }

    @Test
    @DisplayName("Delete existing accommodation")
    void delete_ExistingAccommodation_ShouldDeleteAccommodation() {
        Long id = 1L;

        when(accommodationRepository.existsById(id)).thenReturn(true);

        accommodationService.deleteById(id);
    }

    @Test
    @DisplayName("Delete a non-existing accommodation")
    void delete_NonExistingAccommodation_ShouldDeleteAccommodation() {
        Long id = 999L;

        EntityNotFoundException actualMessage = assertThrows(
                EntityNotFoundException.class, () -> accommodationService.deleteById(id));

        String expectedMessage = "Accommodation not found with id: " + id;

        assertEquals(expectedMessage, actualMessage.getMessage());
    }
}
