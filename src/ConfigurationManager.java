
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class used to create ConfigurationFile object from the config file sent in as a parameter
 */
public class ConfigurationManager {


    /**
     * Creates ConfigurationFile from the config file specified as arguments
     *
     * @param file Filename of configuration file
     * @param commands
     * @return Newly made configuration file
     */
	static ConfigurationFile createConfig(String file, String commands) {
		int hostPort = 0;
		char hostCharacter;
		ConfigurationFile config;

		File configFile = new File(file);
		Scanner input = null;
		try {
			input = new Scanner(configFile);
		} catch (FileNotFoundException e) {

			System.err.println("Could not open file - " + file);
			e.printStackTrace();
			System.exit(1);
		}
		


		String hostInput = input.nextLine();
		hostCharacter = hostInput.charAt(0);
		hostPort = Integer
				.parseInt(hostInput.substring(hostInput.indexOf(":") + 1));
		
		config = new ConfigurationFile(hostCharacter, hostPort, commands);
		

		
		while(input.hasNext()) {
			String line = input.nextLine();
			String[] split = line.split("-");
			char clientName = line.charAt(0);
			int clientPort = Integer.parseInt(split[1]);
			int clientDelay = Integer.parseInt(split[2]);
			config.add(new ServerInfo(clientName, clientPort, clientDelay));

		}
		
		input.close();
		
		return config;
	}
}