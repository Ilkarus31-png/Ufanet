
package ufanet_pool.request.model;

public class CancelReservationRequest {

    private Long clientId;
    private String reason;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
