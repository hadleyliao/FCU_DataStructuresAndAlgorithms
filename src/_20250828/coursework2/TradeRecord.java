package _20250828.coursework2;

public class TradeRecord {
    private final String date; // yyyy/MM/dd
    private final String time; // HH:mm
    private final double open;
    private final double close;
    private final double high;
    private final double low;
    private final long volume;

    public TradeRecord(String date, String time, double open, double close, double high, double low, long volume) {
        this.date = date;
        this.time = time;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public long getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "TradeRecord{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", volume=" + volume +
                '}';
    }
}
