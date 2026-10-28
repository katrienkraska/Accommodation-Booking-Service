package org.example.repository;

import org.example.model.booking.Booking;
import org.example.model.booking.BookingStatusName;
import org.example.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByAccommodationIdAndCheckOutDateAfterAndCheckInDateBefore(
            Long accommodationId,
            LocalDate checkIn,
            LocalDate checkOut);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatusName status);

    List<Booking> findByUser(User user);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(BookingStatusName status);

    @Query("SELECT COUNT(b) FROM Booking b "
            + "WHERE b.accommodation.id = :accommodationId "
            + "AND NOT b.status = 'CANCELED' "
            + "AND NOT b.status = 'EXPIRED' "
            + "AND ("
            + "(:checkInDate BETWEEN b.checkInDate AND b.checkOutDate "
            + "OR :checkOutDate BETWEEN b.checkInDate AND b.checkOutDate) "
            + "OR (b.checkInDate BETWEEN :checkInDate AND :checkOutDate "
            + "OR b.checkOutDate BETWEEN :checkInDate AND :checkOutDate))")
    Long isDatesAvailableForAccommodation(@Param("accommodationId") Long accommodationId,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);

    @Query("select b from Booking b "
            + "where b.checkInDate = :checkInDate and b.status = :statusName")
    List<Booking> findAllByCheckInDate(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("statusName") BookingStatusName statusName);

    @Query("select b from Booking b "
            + "where b.createdAt < :dateTime "
            + "and b.status = :statusName")
    List<Booking> findAllByCreatedAtAndStatus(
            @Param("dateTime") LocalDateTime checkOutDate,
            @Param("statusName") BookingStatusName statusName);

    @Query("select b from Booking b "
            + "where b.checkOutDate = :checkOutDate "
            + "and b.status = :statusName")
    List<Booking> findByCheckOutDateAndStatus(
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statusName") BookingStatusName statusName);
}
