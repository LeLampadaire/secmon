package daemon;

public class MemoryBlock {
    private String url;
    private String state;

    public MemoryBlock(final String url, final String state) {
        this.url = url;
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }
}
