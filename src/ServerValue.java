/**
 * Used in the HashTable of Keys and Values
 * This Object holds both the value and the timestamp of a certain value
 */
public class ServerValue  implements java.io.Serializable {
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
