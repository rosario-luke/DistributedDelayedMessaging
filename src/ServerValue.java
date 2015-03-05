/**
 * Created by Lucas Rosario on 3/5/2015.
 */
public class ServerValue {
    private int value;
    private long timestamp;

    public ServerValue(int v, long t){
        value = v;
        timestamp = t;
    }

    public int getValue(){ return value;}
    public long getTimestamp(){ return timestamp;}

    public void setValue(int v){
        value = v;
    }

    public void setTimestamp(long t){
        timestamp = t;
    }
}
