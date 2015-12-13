package pl.nkg.notifier.events;

public class StatusUpdatedEvent {
    private boolean pending;

    public StatusUpdatedEvent() {
    }

    public StatusUpdatedEvent(boolean pending) {
        this.pending = pending;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }
}
