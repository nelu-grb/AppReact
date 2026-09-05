package com.andesstay.reservations.repository;

import com.andesstay.reservations.model.Reservation;
import com.andesstay.reservations.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:fromDate IS NULL OR r.startDate >= :fromDate) AND " +
           "(:toDate IS NULL OR r.endDate <= :toDate)")
    List<Reservation> findByFilters(
            @Param("status") ReservationStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}