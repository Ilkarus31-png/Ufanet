
package ufanet_pool.request.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ufanet_pool.request.model.AvailableTimeSlot;
import ufanet_pool.request.model.CancelReservationRequest;
import ufanet_pool.request.model.ReservationCreatedResponse;
import ufanet_pool.request.model.ReservationInfo;
import ufanet_pool.request.model.ReservationRequest;
import ufanet_pool.request.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<AvailableTimeSlot> findAvailableSlots(LocalDate date)
            throws Exception {
        return reservationRepository.findAvailableSlots(date);
    }

    public List<ReservationInfo> findReservationsByDate(LocalDate date)
            throws Exception {
        return reservationRepository.findReservationsByDate(date);
    }

    public ReservationCreatedResponse createReservation(
            ReservationRequest request) throws Exception {

        if (request.getClientId() == null
                || request.getClientId() <= 0
                || request.getDatetime() == null
                || request.getDatetime().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать clientId и datetime");
        }

        LocalDateTime datetime;

        try {
            datetime = LocalDateTime.parse(request.getDatetime());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата и время должны быть в формате ГГГГ-ММ-ДДTЧЧ:ММ:СС");
        }

        Long id = reservationRepository.createReservation(
                request.getClientId(), datetime);

        return new ReservationCreatedResponse(id);
    }


    public void cancelReservation(
            Long reservationId, CancelReservationRequest request)
            throws Exception {

        if (request.getClientId() == null
                || request.getClientId() <= 0
                || request.getReason() == null
                || request.getReason().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Необходимо указать clientId и причину отмены");
        }

        reservationRepository.cancelReservation(
                reservationId,
                request.getClientId(),
                request.getReason());
    }

}
