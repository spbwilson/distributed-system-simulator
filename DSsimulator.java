//@author Seán

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class DSsimulator {

    public List<Node> nodeList;
    public List<Link> linkList;
    public List<Command> commandList;
    
    //--------------------------------------------------------------------------
    public static void main(String[] args) {
        DSsimulator simulator = new DSsimulator();
        simulator.parseInput();
        simulator.getCommand();
        simulator.printNodeTables();
        simulator.setLinkFail();
        simulator.printNodeTables();
    }
    
    //--------------------------------------------------------------------------
    public void printNodeTables() {
        //This will print each node's table
        
        ArrayList<String[]> table;
        
        System.out.println("Node tables:");
        
        for (int i = 0; i < nodeList.size(); i++) {
            table = nodeList.get(i).getTable();
            
            //Print node name
            System.out.print("table " + nodeList.get(i).getName() + " ");
            //Print each address route
            for (int j = 0; j < table.size(); j++) {
                System.out.print("(" + table.get(j)[0] + "|" + table.get(j)[1] + "|" + table.get(j)[2] + ") ");
            }
            System.out.println();
        }
        System.out.println();
    }    
    
    //--------------------------------------------------------------------------
    public int getNodeIndex(String nodeName) {
        //This is needed to get the index of a node
        int index = -1;
        
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getName().equals(nodeName)) {
                return i;
            }
        }       
        return index;
    }
    
    //--------------------------------------------------------------------------
    public void getCommand() {
        Command currentC;
        
        while(commandList.isEmpty() != true) {
            //Get next command from command queue
            currentC = commandList.get(0);
            
            //If command is a "send", send out nodes table across links
            if (currentC.getType().toLowerCase().equals("send")) {
                //Send Command
                //If is initialiseation command, get initial table of node
                if(currentC.getTable().isEmpty()) {
                    executeSendCommand(currentC.getNode(), nodeList.get(getNodeIndex(currentC.getNode())).getTable());
                }
                else {
                    executeSendCommand(currentC.getNode(), currentC.getTable());
                }
            }
            //Receive command
            else if (currentC.getType().toLowerCase().equals("receive")) {
                executeReceiveCommand(currentC.getNode(), currentC.getFrom(), currentC.getTable());
            }
            //Link-fail command
            else if (currentC.getType().toLowerCase().equals("link-fail")) {
                executeLinkFailCommand(currentC.getNode(), currentC.getFrom());
            }
		    //Remove command from queue
		    commandList.remove(0);
		    
		    //Check for any infinite pointer loops for each address
		    checkForLoops();
        }
        System.out.println("Network Stable\n");
    }
    
    //--------------------------------------------------------------------------
    public void checkForLoops() {
        //For each node
        for (int i = 0; i < nodeList.size(); i++) {
            ArrayList<String[]> nodeTable = new ArrayList<String[]>();
            nodeTable = nodeList.get(i).getTable();
            
            //Check each address for loops
            for (int j = 0; j < nodeTable.size(); j++) {
                //Will hold all previously seen nodes
                ArrayList<String> previouslySeen = new ArrayList<String>();
                
                //Address to be followed
                String address = nodeTable.get(j)[0];
                
                //Add the start node as seen, set link
                String start = nodeList.get(i).getName();
                String link = nodeTable.get(j)[1];
                previouslySeen.add(start);               
            
                //If the next node in the route is not broken or local then recurse
                while (true != (link.equals("no-link") || link.equals("local"))) {
                    previouslySeen.add(link);
                    link = nodeList.get(getNodeIndex(link)).returnLink(address);
                    
                    //If the next node has been seen before, there is a loop
                    for (int k = 0; k < previouslySeen.size(); k++) {
                        if (link.equals(previouslySeen.get(k))) {
                            System.out.println("Loop Found for address " + address + " here starting at node " + nodeList.get(i).getName());
                            break;
                        }
                    }
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    public void executeSendCommand(String senderNode, ArrayList<String[]> sentTable) {
        //This EXECUTES send command and CREATES receive commands
                
        //For each link attached to node, print send message and create a receive command 
        for (int i = 0; i < linkList.size(); i++) {
            if (linkList.get(i).getEndOne().equals(senderNode)) {
                
                //Print the send command
                System.out.print("send " + senderNode + " " + linkList.get(i).getEndTwo());
                for (int j = 0; j < sentTable.size(); j++) {
                    System.out.print(" (" + sentTable.get(j)[0] + "|" + sentTable.get(j)[1] + "|" + sentTable.get(j)[2] + ")");
                }
                System.out.println();
                
                //Add a receive command
                commandList.add(new Command("receive", linkList.get(i).getEndTwo(), senderNode, sentTable));
            }
            else if (linkList.get(i).getEndTwo().equals(senderNode)) {
                
                //Print the send command
                System.out.print("send " + senderNode + " " + linkList.get(i).getEndOne());
                for (int j = 0; j < sentTable.size(); j++) {
                    System.out.print(" (" + sentTable.get(j)[0] + "|" + sentTable.get(j)[1] + "|" + sentTable.get(j)[2] + ")");
                }
                System.out.println();
                
                //Add a receive command
                commandList.add(new Command("receive", linkList.get(i).getEndOne(), senderNode, sentTable));
            }
        }
    }
    
    //--------------------------------------------------------------------------
    public void executeReceiveCommand(String receiverNode, String senderNode, ArrayList<String[]> receivedTable) {
        //This EXECUTES receive commands and CREATES send commands
        ArrayList<String[]> table = new ArrayList<String[]>();
        
        //Get node index
        int index = getNodeIndex(receiverNode);
        
        //Print each address route
        System.out.print("receive " + receiverNode + " " + senderNode);
        for (int i = 0; i < receivedTable.size(); i++) {
            System.out.print("(" + receivedTable.get(i)[0] + "|" + receivedTable.get(i)[1] + "|" + receivedTable.get(i)[2] + ") ");
        }
        System.out.println();
        
        //Execute the routing information protocol, returns true if updates are made 
        Boolean updatesMade = nodeList.get(index).RIP(senderNode, receivedTable);
        
        //If the received table updated nodes table, broadcast over link
        if (updatesMade == true) {
            table = nodeList.get(index).getTable();
            commandList.add(new Command("send", receiverNode, table));
        }
    }
    
    //--------------------------------------------------------------------------
    public void executeLinkFailCommand(String nodeOne, String nodeTwo) {
        ArrayList<String[]> nodeOneTable = new ArrayList<String[]>();
        ArrayList<String[]> nodeTwoTable = new ArrayList<String[]>();
        int nodeOneIndex = getNodeIndex(nodeOne);
        int nodeTwoIndex = getNodeIndex(nodeTwo);
        System.out.println(nodeOne + " " + nodeTwo);
        
        //Print the link-fail commands
        System.out.println("link-fail " + nodeOne + " " + nodeTwo);
        System.out.println("link-fail " + nodeTwo + " " + nodeOne);
        
        //Check which link fails and remove it from linkList
        for (int i = 0; i < linkList.size(); i++) {
            if (linkList.get(i).getEndOne().equals(nodeOne) && linkList.get(i).getEndTwo().equals(nodeTwo)) {
                linkList.remove(i);
            }
            else if (linkList.get(i).getEndTwo().equals(nodeOne) && linkList.get(i).getEndOne().equals(nodeTwo)) {
                linkList.remove(i);
            }
        }
        
        //Update routing tables of effected nodes
        nodeOneTable = nodeList.get(nodeOneIndex).getTable();
        nodeTwoTable = nodeList.get(nodeTwoIndex).getTable();
        
        //As it's a connected network, both tables will be same length
        for (int i = 0; i < nodeOneTable.size(); i++) {
            if (nodeOneTable.get(i)[1].equals(nodeTwo)) {
                nodeOneTable.set(i, new String[]{nodeOneTable.get(i)[0],"no-link","i"});
            }
            if (nodeTwoTable.get(i)[1].equals(nodeOne)) {
                nodeTwoTable.set(i, new String[]{nodeTwoTable.get(i)[0],"no-link","i"});
            }
        }
        nodeOneTable = nodeList.get(nodeOneIndex).getTable();
        nodeTwoTable = nodeList.get(nodeTwoIndex).getTable();
        
        //Send updated table over working links
        commandList.add(new Command("send", nodeOne, nodeOneTable));
        commandList.add(new Command("send", nodeTwo, nodeTwoTable));
    }
    
    //--------------------------------------------------------------------------
    public void setLinkFail() {
        //Here a link-fail is added to the end of the command queue
        commandList.add(new Command("link-fail", "p1", "p2"));
        getCommand();
    }
    
    //--------------------------------------------------------------------------
    public void parseInput() {
        
        nodeList = new LinkedList<Node>(); 
        linkList = new LinkedList<Link>();
        commandList = new LinkedList<Command>();
        
        //Create each new node from the network description 
        nodeList.add(new Node("p1", new int[]{1}));
        nodeList.add(new Node("p2", new int[]{2}));
        nodeList.add(new Node("p3", new int[]{3}));
        nodeList.add(new Node("p4", new int[]{4,5}));
        

        //Create each new link from the network description
        linkList.add(new Link("p1", "p2"));
        linkList.add(new Link("p1", "p4"));
        linkList.add(new Link("p2", "p3"));
        linkList.add(new Link("p3", "p4"));
        
        //Create the inital commands  
        commandList.add(new Command("send", "p1"));
    }
}    
/*Student: Sean Wilson
Number: s0831408

----------------------
Running the simulator:
----------------------
The program can be compiled using the following command:
javac Command.java Link.java Node.java DSsimulator.java

The program can then be run using the following command:
java DSsimulator

The RIP algorithm is located in the Node class line 29, with method name RIP



--------------
Program Input:
--------------
The network informtion is input as stated in the coursework notes without using parsing in the input
method parseInput() on 242 of DSsimulator.java.
If you wish to change the starting command please edit line 262 of the DSsimulator class.



----------------------
The link-fail command:
----------------------
The link fail command is added to the command queue after the network has become stable. If
you wish to change which link breaks, please edit line 238 of DSsimulator class. The command
format is the same as that in the coursework notes. More than one link-fail command can be
added if you so wish.



-------------------
Part 1 & 2 Results:
-------------------
Part 1 final table:
table p1 (1|local|0) (2|p2|1) (4|p4|1) (5|p4|1) (3|p2|2) 
table p2 (2|local|0) (1|p1|1) (4|p1|2) (5|p1|2) (3|p3|1) 
table p3 (3|local|0) (2|p2|1) (1|p2|2) (4|p4|1) (5|p4|1) 
table p4 (4|local|0) (5|local|0) (1|p1|1) (2|p1|2) (3|p3|1)

Part 2 final table:
table p1 (1|local|0) (2|p4|3) (4|p4|1) (5|p4|1) (3|p4|2) 
table p2 (2|local|0) (1|p3|3) (4|p3|2) (5|p3|2) (3|p3|1) 
table p3 (3|local|0) (2|p2|1) (1|p4|2) (4|p4|1) (5|p4|1) 
table p4 (4|local|0) (5|local|0) (1|p1|1) (2|p3|2) (3|p3|1) 

These results can also be seen in a comment at the bottom of DSsimulator.java



--------------------------
Looping code and solution:
--------------------------
A method was written to assist in finding a potential loop for an address in the network. This
can be found starting on line 91 of DSsimulator class, the method is named checkForLoops().

An example of when a loop occurs is explained below:
If we have the following network:

	//Create each new node from the network description 
	nodeList.add(new Node("p1", new int[]{1}));
	nodeList.add(new Node("p2", new int[]{2}));
	nodeList.add(new Node("p3", new int[]{3}));
	nodeList.add(new Node("p4", new int[]{4}));


	//Create each new link from the network description
	linkList.add(new Link("p1", "p2"));
	linkList.add(new Link("p2", "p3"));
	linkList.add(new Link("p3", "p4"));

	//Create the inital commands  
	commandList.add(new Command("send", "p1"));
	
Once the network converges to the correct state for each table, we need to break the two end links
Link("p1", "p2") and Link("p3", "p4"). The following events occur to make a looping condition:

(1) At this point node p2 will update its own table with an infinite cost of reaching address 1. 
(2) p2 sends the updated table to p3
(3) At the same time p3 is updating its table that address 4 is no longer reachable also.
(4) p3 sends the updated table to p2
(5) p3 updates its table with p2's infinite cost for address 1 and sends the table again to p2
(6) p2 receives the first table from p3 with the infinite cost for 4, but also with the cost for
address 1 - e.g. (1|p2|2). This is added to p2's table as (1|p3|3), and p2 sends again

At this point, before p3's updated table reaches p2, there is a loop for address 1 between nodes p2
and p3. Thus, for a loop for an address in a network to occur, two link-fails must occur which 
begins the updating process between two nodes equidistant from the two link-fails.

The method written to detect a loop followed this assumption by checking the state of the node
tables after each command is executed. The suedo code for checkForLoops() is below:

for each Node in NodeList
    for each Address in NodeTable
        add start node to array previouslySeen
        link <- start nodes link for address
        while link is not broken or local
            add link to previouslySeen
            link <- node(link)'s link for address
            if link is in previouslySeen
                print loop occurance
            }
        }
    }
}



------------------------
Interesting Observation:
------------------------
In a triangular network a problem has been found with the RIP algorithm after a link-fail command
where by the network thinks that the tables are up to date, but they are in fact not correct. To 
see this example in action, please use the following input:

	//Create each new node from the network description 
	nodeList.add(new Node("p1", new int[]{1}));
	nodeList.add(new Node("p2", new int[]{2}));
	nodeList.add(new Node("p3", new int[]{3}));


	//Create each new link from the network description
	linkList.add(new Link("p1", "p2"));
	linkList.add(new Link("p2", "p3"));
	linkList.add(new Link("p3", "p1"));

	//Create the inital commands  
	commandList.add(new Command("send", "p1"));

The table converges correctly originally, but when the link-fail command is executed, the two nodes 
at either end of the link failure do not get updated properly. This is because the receiving node 
will not need to update the cost of its own table as its links are unaffected by the link failure. 
Thus it will not send its table back to the other nodes in the network. This may be incorrect 
interpretation of the RIP algorithm, but the solution to this problem is to add a forth condition to
the RIP algorithm:

if the current address from sender table is already in current table
    if the sent cost is infinite and the current table does not link to the sender for the address
        send your current table to the sender
        
This additional statement will ensure that the table of the receiver is sent out regardless of any
updates to its own table.*/
