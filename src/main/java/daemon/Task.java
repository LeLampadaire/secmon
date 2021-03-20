package daemon;

public class Task {
    private String protocol;
    private String message;
    private String status;

    public Task(final String protocol, final String message) {
        this.protocol = protocol;
        this.message = message;
        this.status = "";
    }

    public String getProtocol() {
        return protocol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
}
