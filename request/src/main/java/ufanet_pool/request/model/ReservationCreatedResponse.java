
package ufanet_pool.request.model;

public class ReservationCreatedResponse {

    private final String id;

    public ReservationCreatedResponse(Long id) {
        this.id = String.valueOf(id);
    }

    public String getId() {
        return id;
    }
}
