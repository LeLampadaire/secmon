package models;

public interface ValueData {

    double getValue();
    void setValue(final double value);

    double getMaxValue();
    void setMaxValue(final double maxValue);

    double getMinValue();
    void setMinValue(final double minValue);

    long getTimeRefresh();
    void setTimeRefresh(final long timeRefresh);
}
