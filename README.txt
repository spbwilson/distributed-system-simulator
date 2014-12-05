Student: Sean Wilson
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
updates to its own table.
