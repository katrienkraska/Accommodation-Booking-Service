package org.example.dto.booking;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private LocalDateTime createAt;

    private Long accommodationId;
    private Long userId;

    private String status;
}
