

public class ServerInfo{
	
	private char Identifier;
	private int PortNumber;
	private int PortDelay;
	
	public ServerInfo(char sLetter, int portNum, int delay){
		Identifier = sLetter;
		PortNumber = portNum;
		PortDelay = delay;
	}
	
	public char getIdentifier(){
		return Identifier;
	}
	
	public int getPortNumber(){
		return PortNumber;
	}
	
	public int getPortDelay(){
		return PortDelay;
	}

	
}