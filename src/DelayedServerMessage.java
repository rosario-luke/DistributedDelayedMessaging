import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedServerMessage implements Delayed {

	private ServerInfo serverInfo;
	private Command message;
	private long startTime;
    private boolean isReadSeq;

	public DelayedServerMessage(ServerInfo si, Command m, long delay) {
		serverInfo = si;
		message = m;
		startTime = System.currentTimeMillis() + delay;
        isReadSeq = false;

	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public Command getMessage() {
		return message;
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