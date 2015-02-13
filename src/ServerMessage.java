import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ServerMessage implements Delayed {

	private ServerInfo serverInfo;
	private String message;
	private long startTime;

	public ServerMessage(ServerInfo si, String m, long delay) {
		serverInfo = si;
		message = m;
		startTime = System.currentTimeMillis() + delay;

	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int compareTo(Delayed o) {
		if (this.startTime < ((ServerMessage) o).startTime) {
			return -1;
		}
		if (this.startTime > ((ServerMessage) o).startTime) {
			return 1;
		}
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = startTime - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);

	}
}