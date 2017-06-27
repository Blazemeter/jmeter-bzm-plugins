package Logic;

/**
 * Created by marcos on 3/15/17.
 */
public class dataFragment {
    private String duration;
    private String tsUri;

    public dataFragment(String _duration, String _tsUri){
        this.duration = _duration;
        this.tsUri = _tsUri;
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTsUri() {
        return tsUri;
    }

    public void setTsUri(String tsUri) {
        this.tsUri = tsUri;
    }




}


