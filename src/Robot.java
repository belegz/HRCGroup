import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private String note = "";

	private Properties props;
	private StanfordCoreNLP pipeline;
	private boolean isTimer = false;
	
	private Scanner sc;
	
	private Action previousMove = Action.DO_NOTHING;
	
	private String[] successPhrases = {"Got it.","Ja ja", "Roger that","Wow!","Excellent!"};
	
	private String name;
	
	private Random r = new Random();
	
	private String[] politeAnswers = {"Thank you so much!","You too!","You are so nice!", "Thank you! I'll continue to work hard"};
	
	private String[] jokes = {"If we shouldn't eat at night, why do they put a light in the fridge?",
							  "What did 0 say to 8? Nice belt!",
							  "What do you call bears with no ears? B.",
							  "Why doesn't the sun go to college? Because it has a million degrees!"
							};
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.name = "";
		this.isTimer = false;
		
	    props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    pipeline = new StanfordCoreNLP(props);


	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
	    Annotation annotation;
	    System.out.print("> ");
	    long startTime = System.nanoTime();
//	    System.out.println("Timer: "+ this.isTimer);
	    sc = new Scanner(System.in); 
        String name = sc.nextLine(); 
        final long duration = System.nanoTime() - startTime;
        double seconds = (double)duration / 1_000_000_000.0;
        if(this.isTimer && seconds > 60){
        	System.out.println("> You've thought for more than 1min, do you need to take a break?");
        }        		
        Random index = new Random();
        
//        this.KeywordSearch(name);
//        System.out.println("> "+this.successPhrases[index.nextInt(5)]);
//	    System.out.println("got: " + name);
        annotation = new Annotation(name);
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    if (sentences != null && ! sentences.isEmpty()) {
	      CoreMap sentence = sentences.get(0);
	      SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
	      //TODO: remove prettyPrint() and use the SemanticGraph to determine the action to be executed by the robot.
	      IndexedWord root = graph.getFirstRoot();
          // type of root
          String type = root.tag();
          String temp = root.originalText().toLowerCase();
          switch (type) {
		          case "VB": if(root.originalText().toLowerCase().equals("move")||root.originalText().toLowerCase().equals("go")){
		        	  this.previousMove = processVerbPhrase(graph, root); 
		        	  System.out.println("> "+this.successPhrases[index.nextInt(5)]);
		        	  return this.previousMove;
		          }else if(temp.equals("give")||temp.equals("tell")){
		        	  return processJoke(graph,root);
		          }
		          else if(temp.equals("turn")){
		        	  return processTimer(graph,root);
		          }else if(temp.equals("take")){
		        	  return processNote(graph,root);
		          }else if(temp.equals("show")){
		        	  return processNote(graph,root);
		          }
		          else{
		        	  return this.KeywordSearch(name);
		          }
		          case "VBP": this.previousMove = processVerbPhrase(graph, root); System.out.println("> "+this.successPhrases[index.nextInt(5)]);return this.previousMove;
		          case "VBG": return processVerbPraising(graph,root);
		          case "VBZ": case "NNP": this.previousMove = processVerbPhraseWithName(graph,root);System.out.println("> "+this.successPhrases[index.nextInt(5)]); return this.previousMove;
		          case "RB": this.previousMove = processAdverbPhrase(graph, root); System.out.println("> "+this.successPhrases[index.nextInt(5)]);return this.previousMove;
		          case "JJ":  this.previousMove = processAdjective(graph,root);System.out.println("> "+this.successPhrases[index.nextInt(5)]);return this.previousMove;
		          case "UH": this.previousMove = processAdverbPhrase(graph, root); System.out.println("> "+this.successPhrases[index.nextInt(5)]);return this.previousMove;
		          case "NN":  return processNNPhrase(graph, root);
		          case "WP":  processNamePhrase(graph,root);break;
//		          case "DT": processDeterminer(graph, root); break;
		          default: return this.KeywordSearch(name);
          }
	    }
//	    System.out.println(this.successPhrases[0]);
	    this.previousMove = Action.DO_NOTHING;
	    
	   
	    return Action.DO_NOTHING;
	}
	
	private Action processNote(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		if(root.originalText().toLowerCase().equals("take")){
			List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
			 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
				 IndexedWord temp = prt.second;
				 String find = temp.originalText().toLowerCase();
				 switch(find){
				 	case "note": 
				 		System.out.println("> What do you want me to remember?");
					 	sc = new Scanner(System.in); 
			            String note = sc.nextLine(); 
			            this.note = note;
			            System.out.println("> I got your note!");
				 }
			 }
		}else if(root.originalText().toLowerCase().equals("show")){
			List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
			 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
				 IndexedWord temp = prt.second;
				 String find = temp.originalText().toLowerCase();
				 switch(find){
				 	case "note": 
				 		if(this.note == "") {
				 			System.out.println("> you do not have a note!");
				 		}else{
				 			System.out.println("> your note is: "+this.note);
				 		}
				 		  
				 }
			
		}
	}
		return Action.DO_NOTHING;
	}

	private Action processTimer(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		boolean isTimed = false;
		boolean isOn = false;
		List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 
				 switch(find){
				 	case "timer": 
				 		List<Pair<GrammaticalRelation,IndexedWord>> s1 = graph.childPairs(temp);
						 for (Pair<GrammaticalRelation,IndexedWord> prt1 : s1){
							 IndexedWord temp1 = prt1.second;
							 String find1 = temp1.originalText().toLowerCase();
							 switch (find1){
							 case "on": isOn = true; this.isTimer = true;System.out.println("> Your timer is turned on!");return Action.DO_NOTHING;
							 case "off": isOn = false; this.isTimer = false; System.out.println("> Your timer is turned off!"); return Action.DO_NOTHING;
							 }
						 }
				 		isTimed = true;break;
				 	case "on": isOn = true; break;
				 	case "off" : isOn = false; break;
				 }
			 
	}
		 if(isTimed&&isOn){
			 System.out.println("> Your timer is turned on!");
			 this.isTimer = true;
		 }else{
			 System.out.println("> Your timer is turned off!");
			 this.isTimer = false;
		 }
		 
		 return Action.DO_NOTHING;
	}

	private Action processJoke(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		
		boolean nameUsed = false;
		boolean isGood = false;
		List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 
				 switch(find){
				 	case "joke":System.out.println("> " +this.jokes[this.r.nextInt(4)]);;
				 }
			 
	}
		 
		 return Action.DO_NOTHING;
	}

	private Action processVerbPraising(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		boolean nameUsed = false;
		boolean isGood = false;
		List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 if(find.equals(this.name)||find.equals("you")){
				 nameUsed = true;
			 }else if(nameUsed){
				 switch(find){
				 	case "well":isGood = true;
				 }
			 }
	}
		 if(nameUsed && isGood){
			 System.out.println("> "+ this.politeAnswers[this.r.nextInt(4)]);
		 }
		 return Action.DO_NOTHING;
	}

	private Action processVerbPhraseWithName(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		boolean nameUsed = false;
		List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 if(find.equals(this.name)){
				 nameUsed = true;
			 }else if(nameUsed){
				 switch(find){
				 	case "up": return Action.MOVE_UP;
					case "down" : return Action.MOVE_DOWN;
					case "left" : return Action.MOVE_LEFT;
					case "right" : return Action.MOVE_RIGHT;
					case "clean": return Action.CLEAN;
					case "again" : return previousMove;
				 }
			 }
	}
		
		return Action.DO_NOTHING;
	}

	private void processNamePhrase(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		 List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 switch (find){
			 
			    case "name":
			    	if(this.name.equals("")){
			    		System.out.println("> I don't have a name. What do you want to call me?");
			    		System.out.print("> ");
			    	    sc = new Scanner(System.in); 
			            String name = sc.nextLine(); 
			            this.name = name;
			            System.out.println("> My name is "+ this.name+" right now!");
			       
			    }else{
			    	System.out.println("> My name is "+ this.name+". Do you want to change my name? (Y/n)");
			    	sc = new Scanner(System.in); 
		            String name = sc.nextLine(); 
		            if(name.equals("Y")){
		            	System.out.println("> What do you want to call me?");
		            	sc = new Scanner(System.in); 
			            name = sc.nextLine(); 
			            this.name = name;
			            System.out.println("> My name is "+ this.name+" right now!");
		            }else{
		            	 System.out.println("> My name is "+ this.name+" right now!");
		            }
		            
				
				}
			 }
	}
}

	private Action KeywordSearch(String name){
		String[] words = name.split(" ");
		Action result = Action.DO_NOTHING;
		String[] propendPhrases = { "I think you want to me to ",
									"I believe you are gonna ",
									"I assume that you want to ",
									"I suppose your next step is to ",
									"I understand that you need to me to"};
		String[] propendNotPhrases = { "I think you want to me not to ",
									"I believe you are not gonna ",
									"I assume that you do not want to ",
									"I suppose your next step is not to ",
									"I understand that you need to me not to"};
		boolean isNot = false;
		boolean isRepeat = false;
		String direction = "";
		for(String temp : words){
			if(temp.equals("up")||temp.equals("down")||temp.equals("left")||temp.equals("right")){
				direction = "go " + temp;
				switch (temp){
				case "up": result = Action.MOVE_UP;break;
				case "down" : result = Action.MOVE_DOWN;break;
				case "left" : result = Action.MOVE_LEFT;break;
				case "right" : result = Action.MOVE_RIGHT;break;
				}
					
			}else if(temp.equals("not")){
				isNot = true;
				System.out.println(propendNotPhrases[this.r.nextInt(5)]+direction );
				this.previousMove = Action.DO_NOTHING;
				return Action.DO_NOTHING;
			}else if(temp.equals("clean")){
				direction = temp;
				result = Action.CLEAN;
			}else if(temp.equals("furthur")||temp.equals("more")){
				isRepeat = true;
			}
		}
		
		 if(isRepeat){
			 if(result == this.previousMove){
				 direction ="repeat previous move!";
				 System.out.println(propendPhrases[this.r.nextInt(5)] +direction );
				 return result;
			 }else{
				 return Action.DO_NOTHING;
			 }
		 }
		
		if(direction ==""){
			return this.clarification();
		}
		
		System.out.println(propendPhrases[0] +direction );
		this.previousMove = result;
		return result;
	}
	
	private Action clarification(){
		String[] clarify = {"I am sorry. I don't understand. Please try another command",
					        "Could you try a more specific command?",
					        "Please be more specific on your command",
					        "Could you provide more details on the direction?",
					        "I did not get you. Can you try something else?",
					        "I am confused about your command. Could you tell me again what you want me to do?",
					        "Still cannot get your idea. Can you try another command later?",
					        "Can you tell more about your idea in the next command?",
					        "I am sorry. I did not get your command. Can you provide additional information?",
					        "I am lost. Can you say more things on the next command?"
					     
							};
		Random r = new Random();
		System.out.println(clarify[r.nextInt(10)]);
		return Action.DO_NOTHING;
	}

	private Action processVerbPhrase(SemanticGraph graph, IndexedWord root) {
		boolean isRepeat = false;
		// TODO Auto-generated method stub
		 Action result = Action.DO_NOTHING;
		 List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 switch (find){
			 
			    case "not": return Action.DO_NOTHING;
				case "up": return Action.MOVE_UP;
				case "down" : return Action.MOVE_DOWN;
				case "left" : return Action.MOVE_LEFT;
				case "right" : return Action.MOVE_RIGHT;
				case "clean": return Action.CLEAN;
				case "again" : return previousMove;
				case "furthur" : isRepeat = true; break;
				
				
				}
			 result = processVerbPhrase(graph,temp);
			 
			 if(result != Action.DO_NOTHING){
				 if(isRepeat){
					 if(result == this.previousMove){
						 return result;
					 }else{
						 return Action.DO_NOTHING;
					 }
				 }
				 return result;
			 }
			 
		 }
		
		return Action.DO_NOTHING;
	}

	private Action processNNPhrase(SemanticGraph graph, IndexedWord root) {
		boolean isRepeat = false;
		boolean isPraise = false;
		boolean isGoodPraise = false;
		// TODO Auto-generated method stub
		 Action result = Action.DO_NOTHING;
		 String rootWord = root.originalText().toLowerCase();
		 switch(rootWord){
		 	case "not": result =  Action.DO_NOTHING;break;
			case "up": result =  Action.MOVE_UP;break;
			case "down" : result =  Action.MOVE_DOWN;break;
			case "left" : result =  Action.MOVE_LEFT;break;
			case "right" : result =  Action.MOVE_RIGHT;break;
			case "clean": result =  Action.CLEAN;break;
			case "again" : result =  previousMove;break;
			case "furthur" : isRepeat = true; break;
			case "job": isPraise = true; break;
		 }
		 
		 List<Pair<GrammaticalRelation,IndexedWord>> s = graph.childPairs(root);
		 for (Pair<GrammaticalRelation,IndexedWord> prt : s){
			 IndexedWord temp = prt.second;
			 String find = temp.originalText().toLowerCase();
			 switch (find){
			 
			    case "not": this.previousMove = Action.DO_NOTHING;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.DO_NOTHING;
				case "up": this.previousMove = Action.MOVE_UP;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.MOVE_UP;
				case "down" : this.previousMove = Action.MOVE_DOWN;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.MOVE_DOWN;
				case "left" : this.previousMove = Action.MOVE_LEFT;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.MOVE_LEFT;
				case "right" : this.previousMove = Action.MOVE_RIGHT;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.MOVE_RIGHT;
				case "clean": this.previousMove = Action.CLEAN;System.out.println("> "+this.successPhrases[this.r.nextInt(5)]);return Action.CLEAN;
				case "again" : return previousMove;
				case "furthur" : isRepeat = true; break;
				case "nice": isGoodPraise = true; break;
				case "good": isGoodPraise = true; break;
				case "bad": isGoodPraise = false; break;
				
				}
			 
			 if(isRepeat){
				 if(result == this.previousMove){
					 this.previousMove = result;
					 return result;
				 }else{
					 return Action.DO_NOTHING;
				 }
			 }
			 result = processVerbPhrase(graph,temp);
			 
			 if(result != Action.DO_NOTHING){
				 this.previousMove = result;
				 return result;
			 }
			 
		 }
		 
		 if(isPraise && isGoodPraise){
			 System.out.println("> "+ this.politeAnswers[this.r.nextInt(4)]);
		 }
		
		return Action.DO_NOTHING;
	}
	
	private Action processAdjective(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		String move = root.originalText().toLowerCase();
		switch (move){
		case "up": this.incPosCol();return Action.MOVE_UP;
		case "down" : return Action.MOVE_DOWN;
		case "left" : return Action.MOVE_LEFT;
		case "right" : return Action.MOVE_RIGHT;
		case "clean": return Action.CLEAN;
		}
		return  Action.DO_NOTHING;
	}

	private Action processAdverbPhrase(SemanticGraph graph, IndexedWord root) {
		// TODO Auto-generated method stub
		String move = root.originalText().toLowerCase();
		switch (move){
		case "up": this.incPosCol();return Action.MOVE_UP;
		case "down" : return Action.MOVE_DOWN;
		case "left" : return Action.MOVE_LEFT;
		case "right" : return Action.MOVE_RIGHT;
		case "not" : return Action.DO_NOTHING;
		case "again": return this.previousMove;
		}
		return  Action.DO_NOTHING;
	}
	
	
	



}