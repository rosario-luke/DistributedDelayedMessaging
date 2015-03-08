import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Class that is used as the element for our DelayQueue
 * Holds a command and allows itself to be taken from the DelayQueue when it's delay has passed
 */
public class DelayedServerMessage implements Delayed {

	private ServerInfo serverInfo;
	private Command command;
	private long startTime;
    private boolean isReadSeq;
    private Response response;

	public DelayedServerMessage(ServerInfo si, Command m, long delay) {
		serverInfo = si;
		command = m;
		startTime = System.currentTimeMillis() + delay;
        isReadSeq = false;
        response = null;
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public Command getCommand() {
		return command;
	}

    public void setResponse(Response r){
        response =r ;
    }

    public String getMessage(){
        if(response == null){
            return command.toString();
        } else {
            return response.toString() + "::" + command.toString();
        }
    }

	@Override
	public int compareTo(Delayed o) {
		if (this.startTime < ((DelayedServerMessage) o).startTime) {
			return -1;
		}
		if (this.startTime > ((DelayedServerMessage) o).startTime) {
			return 1;
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = startTime - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);

	}

    public void setReadSeq(boolean b){
        isReadSeq = b;
    }

    public boolean isReadSeq(){
        return isReadSeq;
    }
}