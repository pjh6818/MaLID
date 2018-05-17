package malid.datacollector.Helpers;

public class historyitem {

    private String idx;
    private String time;
    private String heartrate;
    private String classs;

    public String getidx() {
        return idx;
    }
    public void setidx(String idx) {
        this.idx=idx;
    }

    public String gettime() {
        return time;
    }
    public void setTime(String time) { this.time = time; }

    public String getheartrate() {
        return heartrate;
    }

    public void setHeartrate(String heartrate) {
        this.heartrate = heartrate;
    }

    public String getClasss() {
        return classs;
    }

    public void setClasss(String classs) {
        this.classs = classs;
    }
}