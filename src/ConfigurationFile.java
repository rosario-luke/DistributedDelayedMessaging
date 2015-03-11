import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds all the information about the current servers running as well as what port
 * this server is running on, it's name, and the command filename specified
 */
public class ConfigurationFile{
	
	private ArrayList<ServerInfo> myServers;
	private char hostCharacter;
	private int hostPort;
    private String commandFile;
	
	public ConfigurationFile(char h, int p, String c){
		myServers = new ArrayList<ServerInfo>();
		hostCharacter = h;
		hostPort = p;
        commandFile = c;
	}
	
	public void add(ServerInfo info){
		myServers.add(info);
	}
	
	public char getHostIdentifier(){ 
		return hostCharacter;
	}
	
	public int getHostPort(){
		return hostPort;
	}
	
	public ServerInfo findInfoByIdentifier(char id){
		for(int i =0; i<myServers.size();i ++){
			if(myServers.get(i).getIdentifier() == id){
				return myServers.get(i);
			}
		}
		return null;
	}

    public int getNumberOfServers(){
        return myServers.size();
    }

    public String getCommandFile(){ return commandFile;}
    public boolean hasCommandFile(){ return commandFile != null;}
	
	public Iterator<ServerInfo> getServerIterator(){
		return myServers.iterator();
	} 
}