package com.meetingroom.monolith.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByBookingDateDescStartTimeAsc(Long userId);

    List<Booking> findByRoomIdAndBookingDate(Long roomId, LocalDate bookingDate);

    List<Booking> findByBookingDate(LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId AND b.bookingDate = :date AND b.status != 'Cancelled' AND " +
           "((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);

    Long countByStatus(String status);
}
