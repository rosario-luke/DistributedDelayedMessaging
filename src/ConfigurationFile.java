import java.util.ArrayList;
import java.util.Iterator;

public class ConfigurationFile{
	
	private ArrayList<ServerInfo> myServers;
	private char hostCharacter;
	private int hostPort;
	
	public ConfigurationFile(char h, int p){
		myServers = new ArrayList<ServerInfo>();
		hostCharacter = h;
		hostPort = p;
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
	
	public Iterator<ServerInfo> getServerIterator(){
		return myServers.iterator();
	} 
}