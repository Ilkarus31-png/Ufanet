package ufanet_pool.request.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import ufanet_pool.request.model.AvailableTimeSlot;
import ufanet_pool.request.model.Client;
import ufanet_pool.request.model.ReservationInfo;

@Repository
public class ReservationRepository {

    private final DataSource dataSource;

    public ReservationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<AvailableTimeSlot> findAvailableSlots(LocalDate date)
            throws Exception {

        String sql = """
                SELECT
                    TO_CHAR(
                        wh.open_time + slots.slot * INTERVAL '1 hour',
                        'HH24:MI'
                    ) AS time,
                    GREATEST(10 - COUNT(r.id), 0) AS free_count
                FROM working_hours wh
                CROSS JOIN LATERAL generate_series(
                    0,
                    (
                        EXTRACT(
                            EPOCH FROM (wh.close_time - wh.open_time)
                        ) / 3600
                    )::integer - 1
                ) AS slots(slot)
                LEFT JOIN reservations r
                    ON r.start_time =
                        CAST(? AS date)
                        + wh.open_time
                        + slots.slot * INTERVAL '1 hour'
                    AND r.status = 'ACTIVE'
                WHERE wh.day_of_week =
                    EXTRACT(ISODOW FROM CAST(? AS date))::smallint
                GROUP BY wh.open_time, slots.slot
                ORDER BY slots.slot
                """;

        List<AvailableTimeSlot> slots = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setObject(1, date);
            statement.setObject(2, date);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    slots.add(new AvailableTimeSlot(
                            resultSet.getString("time"),
                            resultSet.getInt("free_count")));
                }
            }
        }

        return slots;
    }

    public List<ReservationInfo> findReservationsByDate(LocalDate date)
            throws Exception {

        String sql = """
                SELECT
                    TO_CHAR(
                        wh.open_time + slots.slot * INTERVAL '1 hour',
                        'HH24:MI'
                    ) AS time,
                    c.id AS client_id,
                    c.name,
                    c.phone,
                    c.email
                FROM working_hours wh
                CROSS JOIN LATERAL generate_series(
                    0,
                    (
                        EXTRACT(
                            EPOCH FROM (wh.close_time - wh.open_time)
                        ) / 3600
                    )::integer - 1
                ) AS slots(slot)
                LEFT JOIN reservations r
                    ON r.start_time =
                        CAST(? AS date)
                        + wh.open_time
                        + slots.slot * INTERVAL '1 hour'
                    AND r.status = 'ACTIVE'
                LEFT JOIN clients c
                    ON c.id = r.client_id
                WHERE wh.day_of_week =
                    EXTRACT(ISODOW FROM CAST(? AS date))::smallint
                ORDER BY slots.slot, c.name
                """;

        Map<String, List<Client>> clientsByTime = new LinkedHashMap<>();

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setObject(1, date);
            statement.setObject(2, date);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String time = resultSet.getString("time");

                    List<Client> clients = clientsByTime.computeIfAbsent(
                            time, key -> new ArrayList<>());

                    long clientId = resultSet.getLong("client_id");

                    if (!resultSet.wasNull()) {
                        Client client = new Client();
                        client.setId(clientId);
                        client.setName(resultSet.getString("name"));
                        client.setPhone(resultSet.getString("phone"));
                        client.setEmail(resultSet.getString("email"));
                        clients.add(client);
                    }
                }
            }
        }

        List<ReservationInfo> reservations = new ArrayList<>();

        for (Map.Entry<String, List<Client>> entry
                : clientsByTime.entrySet()) {

            List<Client> clients = entry.getValue();

            reservations.add(new ReservationInfo(
                    entry.getKey(),
                    clients.size(),
                    clients));
        }

        return reservations;
    }

    public Long createReservation(
            Long clientId, LocalDateTime datetime) throws Exception {

        if (datetime.getMinute() != 0
                || datetime.getSecond() != 0
                || datetime.getNano() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Время записи должно начинаться ровно в начале часа");
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String clientSql = """
                        SELECT id
                        FROM clients
                        WHERE id = ?
                        """;

                try (PreparedStatement statement =
                            connection.prepareStatement(clientSql)) {

                    statement.setLong(1, clientId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Клиент не найден");
                        }
                    }
                }

                String hoursSql = """
                        SELECT open_time, close_time
                        FROM working_hours
                        WHERE day_of_week = ?
                        FOR UPDATE
                        """;

                LocalTime openTime;
                LocalTime closeTime;

                try (PreparedStatement statement =
                            connection.prepareStatement(hoursSql)) {

                    statement.setInt(
                            1, datetime.getDayOfWeek().getValue());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "В этот день бассейн не работает");
                        }

                        openTime = resultSet.getTime("open_time").toLocalTime();
                        closeTime = resultSet.getTime("close_time").toLocalTime();
                    }
                }

                LocalTime requestedTime = datetime.toLocalTime();

                if (requestedTime.isBefore(openTime)
                        || requestedTime.plusHours(1).isAfter(closeTime)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Выбранное время находится за пределами рабочего расписания");
                }

                String countSql = """
                        SELECT COUNT(*)
                        FROM reservations
                        WHERE start_time = ?
                        AND status = 'ACTIVE'
                        """;

                try (PreparedStatement statement =
                            connection.prepareStatement(countSql)) {

                    statement.setTimestamp(
                            1, Timestamp.valueOf(datetime));

                    try (ResultSet resultSet = statement.executeQuery()) {
                        resultSet.next();

                        if (resultSet.getInt(1) >= 10) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "На это время уже записано максимальное количество клиентов");
                        }
                    }
                }

                String insertSql = """
                        INSERT INTO reservations (client_id, start_time)
                        VALUES (?, ?)
                        RETURNING id
                        """;

                Long reservationId;

                try (PreparedStatement statement =
                            connection.prepareStatement(insertSql)) {

                    statement.setLong(1, clientId);
                    statement.setTimestamp(
                            2, Timestamp.valueOf(datetime));

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalStateException(
                                    "Не удалось получить идентификатор записи");
                        }

                        reservationId = resultSet.getLong("id");
                    }
                }

                connection.commit();
                return reservationId;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }


    public void cancelReservation(Long reservationId, Long clientId, String reason)
            throws Exception {

        String sql = """
                UPDATE reservations
                SET status = 'CANCELLED',
                    cancel_reason = ?
                WHERE id = ?
                AND client_id = ?
                AND status = 'ACTIVE'
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reason);
            statement.setLong(2, reservationId);
            statement.setLong(3, clientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Активная запись этого клиента не найдена");
                }
            }
        }
    }

}
