import java.util.ArrayList;

/**
 * Created by Lucas Rosario on 3/5/2015.
 */
public class CommandResponse {



    private ArrayList<Response> responseList;
    private int responsesNeeded;
    private Object cv;
    private Command command;
    public CommandResponse(Command c, int maxNumServers){
        responseList = new ArrayList<Response>();
        command = c;
        cv = new Object();
        switch(c.getModel()){
            case Command.LINEARIZABLE_MODEL:
                responsesNeeded = 1;
                break;
            case Command.SEQUENTIALLY_CONSISTENT_MODEL:
                responsesNeeded = 1;
                break;
            case Command.EVENTUAL_1_MODEL:
                responsesNeeded = 1;
                break;
            case Command.EVENTUAL_2_MODEL:
                responsesNeeded = 2;
                break;
        }
        if(c.getType() == Command.SEARCH_COMMAND){
            responsesNeeded = maxNumServers-1; // TAKE OUT 1 FOR THE SEQUENCER
        }
    }

    public void addResponse(Response r){
        responseList.add(r);
        responsesNeeded--;
        if(responsesNeeded == 0){
            synchronized (cv){
                cv.notify();
            }
        }
    }

    public void waitForResponses(){
        synchronized(cv){
            try{
                cv.wait();
            }catch(InterruptedException e){
                System.out.println("Error waiting for responses");
            }
        }
    }

    public void recieveCommandFromMyself(){
        if(command.isLinearOrSequential()) {
            synchronized (cv) {
                cv.notify();
            }
        }
    }

    public ArrayList<Response> getResponseList() {
        return responseList;
    }
}
