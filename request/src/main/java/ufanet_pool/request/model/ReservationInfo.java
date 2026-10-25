package ufanet_pool.request.model;

import java.util.List;

public class ReservationInfo {

    private final String time;
    private final int count;
    private final List<Client> clients;

    public ReservationInfo(String time, int count, List<Client> clients) {
        this.time = time;
        this.count = count;
        this.clients = clients;
    }

    public String getTime() {
        return time;
    }

    public int getCount() {
        return count;
    }

    public List<Client> getClients() {
        return clients;
    }
}
