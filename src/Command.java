/**
 * Created by Lucas Rosario on 2/15/2015.
 */
public class Command {
    final static int DELETE_COMMAND = 1;
    final static int GET_COMMAND = 2;
    final static int INSERT_COMMAND  = 3;
    final static int UPDATE_COMMAND = 4;
    final static int LINEARIZABLE_MODEL = 1;
    final static int SEQUENTIALLY_CONSISTENT_MODEL = 2;
    final static int EVENTUAL_1_MODEL = 3; // W = 1, R = 1
    final static int EVENTUAL_2_MODEL = 4; // W = 2, R = 2



    private char origin;
    private int type;
    private int key;
    private int value;
    private int model;

    public Command(char o, int t, int k, int v, int m){
        origin = o;
        type = t;
        key = k;
        value = v;
        model = m;
    }

    public String toString(){
        return "Command:origin="+ origin + ",type=" + type + ",key=" + key + ",value=" + value + ",model=" + model;
    }

    public char getOrigin() {
        return origin;
    }

    public int getType() {
        return type;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    public int getModel() {
        return model;
    }
}
