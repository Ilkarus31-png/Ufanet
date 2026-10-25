package ufanet_pool.request.model;

public class AvailableTimeSlot {
    private String time;
    private int count;

    public AvailableTimeSlot(String time, int count) {
        this.time = time;
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public int getCount() {
        return count;
    }
}
