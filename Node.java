//@author Seán
//Imports:

import java.util.ArrayList;


public class Node {

    private String name;
    private ArrayList<String[]> routingTable;
    
    public Node(String s, int[] la) {
        //Store name of node created and local addresses
        name = s;
                
        //Initialise the table (address|link|cost)
        routingTable = new ArrayList<String[]>();       
        for (int i = 0; i < la.length; i++) {
            routingTable.add(new String[]{Integer.toString(la[i]),"local","0"});
        }
    }
    
    //--------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    
    //--------------------------------------------------------------------------
    public Boolean RIP(String senderNode, ArrayList<String[]> senderTable) {
        //This will be set to true if any table updates occur
        Boolean tableUpdated = false;
        
        //For each address in sent table
        for (int i = 0; i < senderTable.size(); i++) {
        	
        	//Keep track of current row, if seen in table, set to true
        	Boolean addressInTable = false;
        	int indexInTable = 0;
        	int senderCost;
        	int currentCost;
        	
        	//Get the cost of sender, if infinite, set to -1
        	try {
                senderCost = Integer.parseInt(senderTable.get(i)[2]);
            } catch (Exception ex) {
                senderCost = -1;
            }
        	
        	//Try and find the address in the current table, if found save index
        	for (int j = 0; j < routingTable.size(); j++) {
        		if (senderTable.get(i)[0].equals(routingTable.get(j)[0])) {
        			addressInTable = true;
        			indexInTable = j;
        		}
        	}
        	
        	//Get current cost, if currentCost is infinite, set to -1
        	try {
        		currentCost = Integer.parseInt(routingTable.get(indexInTable)[2]);
        	} catch (Exception ex) {
        		currentCost = -1;
        	}
        	
        	//Case 1 - If not already in table, add to table
        	if (addressInTable == false) {
        		addTableRoute(new String[]{senderTable.get(i)[0], senderNode, Integer.toString(senderCost + 1)});
        		indexInTable = routingTable.size()-1;
        		currentCost = senderCost + 1;
                tableUpdated = true;
            }
            
            //Case 2 - If cost is better, replace
            if ((senderCost != -1) && ((senderCost+1 < currentCost) || currentCost == -1)) {
            	updateTableRoute(indexInTable, routingTable.get(indexInTable)[0], senderNode, Integer.toString(senderCost + 1));
                currentCost = senderCost + 1;
                tableUpdated = true;
            }
            
            //Case 3 - If address is known via sender already, if not exactly cost -1, update
            if ((routingTable.get(indexInTable)[1].equals(senderNode)) && (currentCost-1 != senderCost)) {
            	//If the sender has a broken link, update info to broken
            	String cost;
            	if (senderCost == -1) {
            		cost = "i";
            		senderNode = "no-link";
				} else {
					cost = Integer.toString(senderCost+1);
				}
            	updateTableRoute(indexInTable, routingTable.get(indexInTable)[0], senderNode, cost);
            	tableUpdated = true;
            }
        }
        return tableUpdated;
    } 
    
    //--------------------------------------------------------------------------
    public void addTableRoute(String[] newAddress) {
        //Add new address to routing table
        routingTable.add(newAddress);
    }
    
    //--------------------------------------------------------------------------
    public void updateTableRoute(int index, String address, String link, String cost) {
        //Find address in list and update cost
        routingTable.set(index, new String[]{address, link, cost});
    }
    
    //--------------------------------------------------------------------------
    public ArrayList<String[]> getTable() {
        //Send table over links to neighbour nodes
        return routingTable;
    }
    
    //--------------------------------------------------------------------------
    public String returnLink(String address) {
        //Return the link for a certain address in the table
        for(int i = 0; i < routingTable.size(); i++) {
            if (routingTable.get(i)[0].equals(address)) {
                return routingTable.get(i)[1];
            }
        }
        return "empty";
    }
}
