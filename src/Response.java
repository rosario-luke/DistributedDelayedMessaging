/**
 * Class for constructing responses and sending them. Allows for easy management
 */
public class Response {

    private char origin;
    private int value;
    private long timestamp;


    public Response(char o, int v, long t){
        origin = o;
        value = v;
        timestamp = t;
    }

    public Response(String s){
        s = s.substring(9);
        String[] split = s.split(",");
        origin = split[0].charAt(7);
        value = Integer.parseInt(split[1].substring(6));
        timestamp = Long.parseLong(split[2].substring(10));
    }

    public String toString(){
        return "Response:origin=" + origin + ",value=" + value + ",timestamp=" + timestamp;
    }


    public char getOrigin() {
        return origin;
    }

    public int getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o){
        if(o == null) { return false;}
        Response ro = (Response) o;
        return this.value == ro.value && this.timestamp == ro.timestamp && this.getOrigin() == ro.getOrigin();
    }
}
