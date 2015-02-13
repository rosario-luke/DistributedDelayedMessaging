import java.util.ArrayList;
import java.util.Iterator;

public class ServerInfoList{
	
	ArrayList<ServerInfo> infoList;
	
	public ServerInfoList(){
		infoList = new ArrayList<ServerInfo>();
	}
	
	public void add(ServerInfo info){
		infoList.add(info);
	}
	
	public ServerInfo findInfoByIdentifier(char id){
		for(int i =0; i<infoList.size();i ++){
			if(infoList.get(i).getIdentifier() == id){
				return infoList.get(i);
			}
		}
		return null;
	}
	
	public Iterator<ServerInfo> getIterator(){
		return infoList.iterator();
	}
	
}