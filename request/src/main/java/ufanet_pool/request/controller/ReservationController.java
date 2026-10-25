package ufanet_pool.request.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ufanet_pool.request.model.AvailableTimeSlot;
import ufanet_pool.request.model.CancelReservationRequest;
import ufanet_pool.request.model.ReservationCreatedResponse;
import ufanet_pool.request.model.ReservationInfo;
import ufanet_pool.request.model.ReservationRequest;
import ufanet_pool.request.service.ReservationService;

@RestController
@RequestMapping("/api/v0/pool")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/time-slots/available")
    public List<AvailableTimeSlot> getAvailableSlots(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) throws Exception {

        return reservationService.findAvailableSlots(date);
    }

    @GetMapping("/reservations")
    public List<ReservationInfo> getReservations(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) throws Exception {

        return reservationService.findReservationsByDate(date);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreatedResponse createReservation(
            @RequestBody ReservationRequest request) throws Exception {

        return reservationService.createReservation(request);
    }

    @PostMapping("/reservations/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(
            @PathVariable Long id,
            @RequestBody CancelReservationRequest request) throws Exception {

        reservationService.cancelReservation(id, request);
    }
}
