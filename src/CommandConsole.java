import sun.plugin2.message.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsole implements Runnable {

    private DelayQueue<DelayedServerMessage> delayQueue;
    private ConfigurationFile config;
    private ConcurrentHashMap<Integer, ServerValue> myTable;
    private String threadName;
    private Thread t;
    private MessageGenerator generator;

    private ConcurrentHashMap<Command, CommandResponse> myCommands;

    public CommandConsole(DelayQueue<DelayedServerMessage> dq, ConfigurationFile con, String name, ConcurrentHashMap<Integer, ServerValue> map, ConcurrentHashMap<Command, CommandResponse> mC, MessageGenerator g) {
        delayQueue = dq;
        config = con;
        generator = g;
        threadName = name;
        myTable = map;
        myCommands = mC;
    }


    /**
     * Opens file reader and waits until user hits "Enter" to continue
     * Reads commands in file if a command file is specified
     * After reading from command file moves on and reads from stdin
     * Parses and executes commands adding them to the delay queue
     */
    public void run() {
        Scanner _input = null;
        String inputLine;

        System.out.println("Press enter to start sending commands");
        _input = new Scanner(System.in);
        _input.nextLine();


        if (config.hasCommandFile()) {
            System.out.println("Sending commands from file");
            File cf = new File(config.getCommandFile());
            Boolean fileFound = false;
            try {
                _input = new Scanner(cf);
                fileFound = true;
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file, skipping to manual input");
            }

            if (fileFound) {
                while (_input.hasNext()) {
                    inputLine = _input.nextLine();
                    System.out.println(inputLine);
                    handleCommand(inputLine);

                }
            }
            _input.close(); // Close File Reader
        }
        _input = new Scanner(System.in);
        // Start asking for commands
        System.out.println("Please enter commands now");

        while (!((inputLine = _input.nextLine()).equals("exit"))) {

            handleCommand(inputLine);
        }
        _input.close();


    }

    public void start() {

        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
    }


    /**
     *
     * @param c - The command to parse
     *          Prints the specialized acknowledgement string for a command
     */
    public void printAcknowledgementForCommand(Command c) {

        switch (c.getType()) {
            case Command.GET_COMMAND:
                if (c.getModel() == Command.LINEARIZABLE_MODEL) {
                    System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
                } else {
                    Response best = analyzeGetResponses(myCommands.get(c).getResponseList());
                    System.out.println("get(" + c.getKey() + ") = (" + best.getValue() + ", " + best.getTimestamp() + ") accepted");
                    for (Response r : myCommands.get(c).getResponseList()) {
                        if (!r.equals(best)) {
                            System.out.println("get(" + c.getKey() + ") = (" + r.getValue() + ", " + r.getTimestamp() + ") not accepted");
                        }
                    }
                }
                break;
            case Command.INSERT_COMMAND:
                System.out.println("Inserted key " + c.getKey());
                break;
            case Command.UPDATE_COMMAND:
                System.out.println("Key " + c.getKey() + " updated to " + c.getValue());
                break;
            case Command.DELETE_COMMAND:
                System.out.println("Key " + c.getKey() + " deleted");
                break;
            case Command.SEARCH_COMMAND:
                System.out.println("Key " + c.getKey() + " found in servers: ");
                for(Response r : myCommands.get(c).getResponseList()){
                    if(r.getValue() != Integer.MIN_VALUE){
                        System.out.println("Server " + r.getOrigin() + " had key");
                    }
                }

        }

    }

    /**
     *
     * @param inputLine - raw input from file or stdin
     *                  Reads the string and parses the command. Runs the command through
     *                  a MessageGenerator, then adds all the messages to the Delay Queue
     *                  The method then blocks until the command has been acknowledged
     */
    public void handleCommand(String inputLine) {

        if(inputLine.toLowerCase().contains("delay")){
            int delay = Integer.parseInt(inputLine.split(" ")[1]);
            try {
                Thread.sleep(delay * 1000);
            } catch(InterruptedException e){
                System.out.println("Error occurred while delaying next command");
            }
            return;

        } else if(inputLine.toLowerCase().contains("search")){
            inputLine = inputLine.toLowerCase();
        } else if(inputLine.toLowerCase().contains("show-all")){
            for(Map.Entry<Integer, ServerValue> ksv : myTable.entrySet()){
                System.out.println("Value (" + ksv.getKey() + ") = " + ksv.getValue().getValue());
            }
            return;
        }

        ArrayList<DelayedServerMessage> mList;
        try {
            mList = generator.GenerateMessageFromCommandString(inputLine);
        } catch (Exception e) {
            System.out.println("Error occured while generating message, skipping to next command");
            return;
        }
        if (mList.size() == 1 && mList.get(0).isReadSeq()) {
            // IF THE COMMAND IS READ AND MODEL IS SEQUENTIALLY CONSISTENT, DON'T TOTAL BROADCAST, JUST OUTPUT VALUE
            Command c = mList.get(0).getCommand();
            if (myTable.get(c.getKey()) != null) {
                System.out.println("get(" + c.getKey() + ") = " + myTable.get(c.getKey()).getValue());
            } else {
                System.out.println("get(" + c.getKey() + ") occurred but value did not exist");
            }
            return;
        } else {
            Command c = mList.get(0).getCommand();
            myCommands.put(c, new CommandResponse(c, config.getNumberOfServers()));
            for (DelayedServerMessage nMessage : mList) {
                delayQueue.add(nMessage);
            }
        }
        myCommands.get(mList.get(0).getCommand()).waitForResponses();
        printAcknowledgementForCommand(mList.get(0).getCommand());

    }


    /**
     *
     * @param responseList - Holds all the responses for the specified get command
     * @return - The response with the most recent timestamp
     */
    public Response analyzeGetResponses(ArrayList<Response> responseList) {
        Response bestResponse = responseList.get(0);
        for (Response r : responseList) {
            if (r.getTimestamp() > bestResponse.getTimestamp()) {
                bestResponse = r;
            }
        }
        return bestResponse;
    }

}