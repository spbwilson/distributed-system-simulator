//@author Seán
//Imports:

import java.util.ArrayList;


public class Command {

    private String commandType;
    private ArrayList<String[]> table;
    private String node;
    private String from;
    
    //Constructor for send commands
    public Command(String ct, String n, ArrayList<String[]> t) {
        commandType = ct;
        table = new ArrayList<String[]>();
        table = t;
        node = n;
    }
    //Constructor for receive commands
    public Command(String ct, String a, String b, ArrayList<String[]> t) {
        commandType = ct;
        table = new ArrayList<String[]>();
        table = t;
        node = a;
        from = b;
    }
    //Constructor for broken-link commands
    public Command(String ct, String a, String b) {
        commandType = ct;
        table = new ArrayList<String[]>();
        node = a;
        from = b;
    }
    //This is the initialiser command constructor
    public Command(String ct, String a) {
        commandType = ct;
        table = new ArrayList<String[]>();
        node = a;       
    }
    
    //--------------------------------------------------------------------------
    public String getType() {
        return commandType;
    }
    
    //--------------------------------------------------------------------------
    public String getNode() {
        return node;
    }
    
    //--------------------------------------------------------------------------
    public String getFrom() {
        return from;
    }
    
    //--------------------------------------------------------------------------
    public ArrayList<String[]> getTable() {
        return table;
    }
}
