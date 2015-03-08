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
        private int uniq;
        private long sendTime = 0;
        public Command(char o, int t, int k, int v, int m, long time){
            origin = o;
            type = t;
            key = k;
            value = v;
            model = m;
            uniq = Integer.parseInt(Long.toString(time).substring(Long.toString(time).length()-4));
            sendTime = time;
        }


        public Command(String encoded){
            encoded = encoded.substring(encoded.indexOf(":") + 1);
            String[] s = encoded.split(",");
            origin = s[0].charAt(7);
            type = Integer.parseInt(s[1].substring(5));
            key = Integer.parseInt(s[2].substring(4));
            value = Integer.parseInt(s[3].substring(6));
            model = Integer.parseInt(s[4].substring(6));
            uniq = Integer.parseInt(s[5].substring(5));
            sendTime = Long.parseLong(s[6].substring(5));
        }

        public long getTimestamp() { return sendTime;}

        public String toString(){
            return "Command:origin="+ origin + ",type=" + type + ",key=" + key + ",value=" + value + ",model=" + model + ",uniq=" + uniq +",time=" + sendTime;
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

        boolean isLinearOrSequential() {
            return getModel() == SEQUENTIALLY_CONSISTENT_MODEL || getModel() == LINEARIZABLE_MODEL;
        }

        @Override
        public boolean equals(Object eq){
            if(eq == null){ return false;}
            return this.toString().equals(eq.toString());
        }

        @Override
        public int hashCode(){
            int hash = 19;
            hash = 89*hash + (key);
            hash = 89*hash + (value);
            hash = 89*hash + (model);
            hash = 89*hash + (origin);
            hash = 89*hash + (type);
            hash = 89*hash + (uniq);
            return hash;
        }
}
