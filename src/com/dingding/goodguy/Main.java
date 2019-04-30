package com.dingding.goodguy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

/*
 * Google Code Challenge
 * Link: https://code.google.com/codejam/contest/2933486/dashboard#s=p0
 * 
 * There are couple of observations of this problem.
 * 1. Every member has at least one conflict party.
 * 2. There could be more than one grouping solution.
 * 3. The order that members add into a group is not important.
 */
public class Main {
	
    //Define some temporary data structures
    HashMap<String, String> groupA = new HashMap<String, String>();
    HashMap<String, String> groupB = new HashMap<String, String>();

    //Used to find conflict members
    HashMap<String, String> conflictLookup = new HashMap<String, String>();
    
    
    //List of persons need to be arranged
    List<String> persons = new ArrayList<String>();
    

    public static void main(String[] args) {
    	String testCase1 = "Dead_Bowie Nyssa_Raatko\n" + 
    			"Animora Lafety_Le_Fei\n" + 
    			"Animora Mothergod\n" + 
    			"Animora Nyssa_Raatko\n" + 
    			"Dead_Bowie Genevieve_Savidge\n" + 
    			"Dead_Bowie Lafety_Le_Fei\n" + 
    			"Animora Genevieve_Savidge\n" + 
    			"Dead_Bowie Mothergod";
    	String testCase2 = "Mephista New_Wave\n" + 
    			"Mephista Ursa\n" + 
    			"Zaladane Mai_Shen\n" + 
    			"Mephista Mai_Shen\n" + 
    			"White_Rabbit Hypnota\n" + 
    			"White_Rabbit New_Wave\n" + 
    			"Ursa Scandal\n" + 
    			"Zaladane New_Wave\n" + 
    			"Ursa Hypnota\n" + 
    			"Zaladane Scandal";
    	Main tester = new Main();
    	//Load test cases
    	Path path = Paths.get("/Users/ywang/Desktop/test01.in");
    		          
    	Stream<String> lines;
		try {
			lines = Files.lines(path);

			int count = 0;
			int caseCount = 0;
			int numOfCaseLine = 0;
			StringBuilder testCase = new StringBuilder();
			boolean caseStart = false;
			Iterator<String> its = lines.iterator();
			while(its.hasNext()) {
				String s = its.next();
				if(caseCount == 0) {
					//First line is the number of test cases
					caseCount++;
				} else {
					if(!caseStart) {
						try {
							numOfCaseLine = Integer.parseInt(s);
							//The next few lines are the same test case
							count = 0;
							caseStart = true;
							testCase = new StringBuilder();
						} catch (Exception e) {
							
						}
					} else {
						testCase.append(s);
						testCase.append("\n");
						count++;
						if(count == numOfCaseLine) {
							if(tester.checkGroup(testCase.toString())) {
								System.out.println("Test Case #" + caseCount + " : Pass") ;
						    } else {
						    	System.out.println("Test Case #" + caseCount + " : Fail") ;
						    }     
							caseStart = false;
							caseCount++;
						}
					}
					
				}
			}			
		    lines.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
 
    }

    public void printGroupMember() {
  
    	StringBuilder sb = new StringBuilder();
    	Iterator<String> its = groupA.keySet().iterator();
    	while(its.hasNext()) {
    		sb.append(its.next());
    		sb.append("\n");
    	}
    	System.out.println("Group A:");
    	System.out.println(sb.toString());

    	sb = new StringBuilder();
    	its = groupB.keySet().iterator();
    	while(its.hasNext()) {
    		sb.append(its.next());
    		sb.append("\n");
    	}
    	System.out.println("\nGroup B :");
    	System.out.println(sb.toString());   	
    }

    /*
     * Test case is a standard format with M number
     * of conflicting pairs.
     * A sample is:
     * Joe_Smith Jane_Doe
     * John_Devil Joe_Smith
     * Peter_Pan Mini_Copper
     */
    public boolean checkGroup(String testCase){

        //Parse testCase, Setup variables
        boolean firstPair = true;
        StringTokenizer tokenizer = new StringTokenizer(testCase, "\n");
        while(tokenizer.hasMoreTokens()){
            String pair = tokenizer.nextToken();

            //Always 2 strings,
            String [] arrOfStr = pair.split(" ");

            //Create the conflict lookup list
            updateConflictList(arrOfStr[0], arrOfStr[1]);

            if(firstPair) {
                //put them into 2 groups
                groupA.put(arrOfStr[0], "");
                groupB.put(arrOfStr[1], "");
                firstPair = false;
            } else {
                //Add persons into the list
                persons.add(arrOfStr[0]);
                persons.add(arrOfStr[1]);
            }
        }

        //Try to group people
        while(!persons.isEmpty()) {
            String person = persons.get(0);
            persons.remove(0);
           // System.out.println(person);
            //The person already assigned
            if(groupA.get(person) != null || groupB.get(person)!=null) {
            	continue;
            }
            
            //1. Check if this person is conflict with both groups
            if (isConflict(groupA, person) && isConflict(groupB, person)) {
                return false;
            }
            //2. If only conflict with A, add to B
            if (isConflict(groupA, person)) {
                if(!addGroup(person, groupB, groupA)) {
                	return false;
                }
            } else if (isConflict(groupB, person)) {
            	//3. If only conflict with B, add to A
            	 if(!addGroup(person, groupA, groupB)) {
                 	return false;
                 }
            } else {
            	//This person is not conflict with either group
            	//First try add to group A
            	boolean testSucess = false;
            	if(testAddGroup(person, groupA, groupB)) {
            		addGroup(person, groupA, groupB);
            		testSucess = true;
            	} else {
            		//Then try add to group B
            		if(testAddGroup(person, groupA, groupB)) {
            			addGroup(person, groupB, groupA);
                		testSucess = true;
            		}
            	}
            	//Both failed, return false;
            	if(!testSucess) {
            		return false;
            	}
            }
        }

        return true;
    }
    
    /*
     * Test if it's possible to add person to a non-conflict group and its conflicts to
     * other group. Use a cloned HashMap in order to avoid alter the original group. 
     * Return true if success.
     */
    public boolean testAddGroup(String person, HashMap<String, String> targetGroup, HashMap<String, String> conGroup) {
    	
    	//Used for check 
    	HashMap<String, String> localGroup = new  HashMap<String, String>();
    	localGroup.putAll(conGroup);
    	
    	String consStr = conflictLookup.get(person);
    	
    	StringTokenizer tokenizer = new StringTokenizer(consStr, "|");
    	while(tokenizer.hasMoreTokens()) {
    		String opp = tokenizer.nextToken().trim();
    		if(!isConflict(localGroup, opp)) {
    			localGroup.put(opp, "");
    		} else {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    /*
     * Try add person to a non-conflict group and its conflicts to
     * other group. 
     * Return true if success.
     */
    public boolean addGroup(String person, HashMap<String, String> targetGroup, HashMap<String, String> conGroup) {
    	
    	targetGroup.put(person, "");
    	
    	String consStr = conflictLookup.get(person);
    	
    	StringTokenizer tokenizer = new StringTokenizer(consStr, "|");
    	while(tokenizer.hasMoreTokens()) {
    		String opp = tokenizer.nextToken().trim();
    		if(!isConflict(conGroup, opp)) {
    			conGroup.put(opp, "");
    		} else {
    			return false;
    		}
    	}
    	
    	return true;
    }

    /*
     * This method is used to create the lookup table of the conflicting
     * members of a member. 
     */
    public void updateConflictList(String person1, String person2) {
       String s = conflictLookup.get(person1);
       if(s != null) {
           s = s + " | " +person2;
       } else {
           s = person2;
       }
       conflictLookup.put(person1, s);

       s = conflictLookup.get(person2);
       if(s != null) {
           s = s + " | " +person1;
       } else {
           s = person1;
       }
       conflictLookup.put(person2, s);

    }

    /*
     * This method check is a member is conflicting with 
     * other members in a group.
     * return false if no conflict.
     */
    public boolean isConflict(HashMap<String, String> group, String person){

        while(group.keySet().iterator().hasNext()){
            String groupMember = group.keySet().iterator().next();
            String conflicts = conflictLookup.get(person);
            if(conflicts != null) {
                if (conflicts.indexOf(groupMember) >= 0) {
                    return true;
                } else {
                	return false;
                }
            }
        }
        return false;
    }
}


