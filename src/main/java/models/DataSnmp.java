package models;

public class DataSnmp implements ValueData{
    final String protocol;
    final String ip;
    final String port;
    private double value = 0;
    private double maxValue = 0;
    private double minValue = 0;
    private long timeRefresh = 0;

    private String oid;
    private String community;

    public DataSnmp(final String protocol, final String ip, final String port){
        this.protocol = protocol;
        this.ip = ip;
        this.port = port;
    }

    public String getOid(){
        return oid;
    }

    public void setOid(final String oid){
        this.oid = oid;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(final String community) {
        this.community = community;
    }

    public String getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getIp() {
        return ip;
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
    public void setTimeRefresh(final long timeRrefresh) {
        this.timeRefresh = timeRrefresh;
    }
}
