package models;

public class DataHttps implements ValueData{
    private String url="";
    private double value = 0;
    private double minValue = 0;
    private double maxValue = 0;
    private long timeRefresh = 0;

    public String getUrl() {
        return url;
    }

    public long getTimeRrefresh() {
        return timeRefresh;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimeRrefresh(long timeRrefresh) {
        this.timeRefresh = timeRrefresh;
    }

    public DataHttps(String url, double minValue, double maxValue, long timeRrefresh){
        this.url = url;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.timeRefresh = timeRrefresh;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void setValue(final double value) {
        this.value = value;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public void setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public double getMinValue() {
        return minValue;
    }

    @Override
    public void setMinValue(final double minValue) {
        this.minValue = minValue;
    }

    @Override
    public long getTimeRefresh() {
        return timeRefresh;
    }

    @Override
    public void setTimeRefresh(final long timeRefresh) {
        this.timeRefresh = timeRefresh;
    }
}
