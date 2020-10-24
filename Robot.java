import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

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

	private Properties props;
	private StanfordCoreNLP pipeline;
	private static String RobotName = "No One";
	private static boolean haveName = false;
	private static boolean turnedOff = false;
	private static String originalString = null;
	private static ArrayList<Action> plan = new ArrayList<>();
	private static boolean startRecording = false;

	private static Action lastAction = Action.DO_NOTHING;
	private static ArrayList<String> ConfirmResponses = new ArrayList<String>(){
		{
			add("Got it.");
			add("Roger that.");
			add("Geronimo!");
			add("Molto bene.");
			add("Speak Mellon and Enter.");
		}
	};

	private static ArrayList<String> KeywordResponses = new ArrayList<String>(){
		{
			add("I think you want me to ");
			add("I believe you want me to ");
			add("I am sure you want me to ");
			add("I know you want me to ");
			add("I understand that you need me to ");
		}
	};

	private static ArrayList<String> ClarificationResponses = new ArrayList<String>(){
		{
			add("Could you please try again?");
			add("Excuse me? Can you please clarify your command?");
			add("I do not understand. Please try again.");
			add("Sorry, I don't get it.");
			add("I'm not sure what you meant by that.");
			add("Could you clarify your intention please?");
			add("I don't understand that command. Can you try again? ");
			add("I didn't get that. Could you try again please?");
			add("I can't understand that command. Can you please try again?");
			add("Entschuldigung. Kannst du das nochmal sagen?");
		}
	};

	private static ArrayList<String> praiseResponses = new ArrayList<String>(){
		{
			add("I know. I am really fantastic, am I not?");
			add("Thank you ever so! You are so kind.");
			add("That's flattering. But we probably need to focus on work. ");
		}
	};

	private static ArrayList<String> hiResponses = new ArrayList<String>(){
		{
			add("Hello to you too. ");
			add("How are you! ");
			add("Oh you saying hi to me? So sweet!");
		}
	};

	private static ArrayList<String> byeResponses = new ArrayList<String>(){
		{
			add("Goodbye. You can turn off now. ");
			add("Bye. You can turn off now. ");
			add("See you! Turning off.");
			add("Auf Wiedersehen.");
		}
	};

	private static ArrayList<String> flatterResponses = new ArrayList<String>(){
		{
			add("It is so kind of you to move me around. I really need to strech my legs.");
			add("Great move. Absolutely fantastic.");
			add("What an advanture. Not much an advanture for you I am sure, but good enough for me.");
		}
	};


	private static Scanner sc;

	/**
	    Initializes a Robot on a specific tile in the environment. 
	 */


	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;

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

		if(turnedOff) {
			return Action.DO_NOTHING;
		}

		if(haveName) {
			System.out.println("Hello, I am the human interaction robot " + RobotName + ". At your service!");
		}else {
			System.out.println("Hello, I am the human interaction robot and I have only a default name. You can choose to give me a name. For now, Robot No One at your service, Valar Morghulis!");
		}

		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in); 
		String name = sc.nextLine(); 
		//			    System.out.println("got: " + name);
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		//		System.out.println(sentences);
		if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			//			System.out.println(sentence);
			this.originalString = sentence.toString();

			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			//DONE: remove prettyPrint() and use the SemanticGraph to determine the action to be executed by the robot.
			//	      graph.prettyPrint();

			IndexedWord root = graph.getFirstRoot();
			String type = root.tag();

			// store last action
			this.lastAction = processPhrase(type,graph,root);
			if(this.lastAction != Action.DO_NOTHING) {
				System.out.println(getRandom(flatterResponses));
			}
			
			if(this.startRecording &&this.lastAction != Action.DO_NOTHING) {
//				System.out.println("Record 1");
				plan.add(this.lastAction);
			}

			return this.lastAction;

		}
		return Action.DO_NOTHING;
	}

	public static Action processPhrase(String type, SemanticGraph graph, IndexedWord root) {
		switch (type) {
		case "VB":
			System.out.println(getRandom(ConfirmResponses));
			return processVerbPhrase(graph, root);
		case "NN": 
			System.out.println(getRandom(ConfirmResponses));
			return processNounPhrase(graph, root);
		case "JJ": 
			System.out.println(getRandom(ConfirmResponses));
			return processAdjectivePhrase(graph, root);
		default: return processDefaultPhrase(graph, root);
		}
	}

	public static String getRandom(ArrayList<String> array) {
		Random random = new Random();
		int index = random.nextInt(array.size());
		return array.get(index);
	}

	public static Action readWordAction(String word) {
		switch(word){
		case "left": return Action.MOVE_LEFT;
		case "right": return Action.MOVE_RIGHT;
		case "up": return Action.MOVE_UP;
		case "down": return Action.MOVE_DOWN;
		case "clean": return Action.CLEAN;
		case "again": return lastAction;
		case "more": return lastAction;
		case "repeat": return lastAction;

		default:
			return Action.DO_NOTHING;
		}
	}

	public static Action readKeyWordAction(String word) {
		switch(word){
		case "left": 
			System.out.println(getRandom(KeywordResponses) + "go left");
			return Action.MOVE_LEFT;
		case "right": 
			System.out.println(getRandom(KeywordResponses) + "go right");
			return Action.MOVE_RIGHT;
		case "up": 
			System.out.println(getRandom(KeywordResponses) + "go up");
			return Action.MOVE_UP;
		case "down": 
			System.out.println(getRandom(KeywordResponses) + "go down");
			return Action.MOVE_DOWN;
		case "clean": 
			System.out.println(getRandom(KeywordResponses) + "clean the block");
			return Action.CLEAN;
			//		case "again": 
			//			System.out.println(getRandom(KeywordResponses) + "repeat last move: " + lastAction.toString());
			//			return lastAction;
			//		case "more": 
			//			System.out.println(getRandom(KeywordResponses) + "repeat last move: "  + lastAction.toString());
			//			return lastAction;
			//		case "repeat": 
			//			System.out.println(getRandom(KeywordResponses) + "repeat last move: "  + lastAction.toString());
			//			return lastAction;

		default: 
			System.out.println(getRandom(ClarificationResponses));
			return Action.DO_NOTHING;
		}
	}

	public static Action processVerbPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("VB");

		if(checkNegate(dependencies, root)) {
			System.out.println("Do nothing");
			return Action.DO_NOTHING;
		}
		
//		if(checkRecord()) {
//			
//		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		
		if(root.originalText().toLowerCase().equals("begin")) {
			String record = s.get(0).second.originalText().toLowerCase();
			if(record.equals("record")) {
				startRecording = true;
				return Action.DO_NOTHING;
			}
		}
		
		if(root.originalText().toLowerCase().equals("stop")) {
			String record = s.get(0).second.originalText().toLowerCase();
			if(record.equals("record")) {
				startRecording = false;
				return Action.DO_NOTHING;
			}
		}
		
		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String word = prt.second.originalText().toLowerCase();
//						System.out.println(word);
			Action temp = readWordAction(word);
			//			System.out.println(temp.toString());
			if(temp != Action.DO_NOTHING) {
				return temp;
			}

		}
		return processDefaultPhrase(dependencies, root);
	}

	public static Action processAdjectivePhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("JJ");
		String word  = root.originalText().toLowerCase();
		if (word.equals("clean")) {
			System.out.println("Cleaning.");
			return Action.CLEAN;
		}
		Action temp = readWordAction(word);
		if(temp != Action.DO_NOTHING) {
			return temp;
		}

		return processDefaultPhrase(dependencies, root);
	}

	public static Action processNounPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("NN");	

		String word  = root.originalText().toLowerCase();
		
		// Temp test code
		if(word.toLowerCase().equals("play")) {
			System.out.println(plan.toString());
			return Action.DO_NOTHING;
		}
		
		Action temp = readWordAction(word);
		if(temp != Action.DO_NOTHING) {
			return temp;
		}

		return processDefaultPhrase(dependencies, root);
	}

	public static Action processDefaultPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Default");	

		if(checkNegate(dependencies, root)) {
			System.out.println("Do nothing");
			return Action.DO_NOTHING;
		}

		if(checkHi(dependencies, root)) {
			System.out.println(getRandom(hiResponses));
			return Action.DO_NOTHING;
		}

		if(checkBye(dependencies, root)) {
			System.out.println(getRandom(byeResponses));
			turnedOff = true;
			return Action.DO_NOTHING;
		}

		if(checkPraise(dependencies, root)) {
			System.out.println(getRandom(praiseResponses));
			return Action.DO_NOTHING;
		}


		if(checkName(dependencies, root)) {
			if(!haveName) {
				System.out.println("I am so happy! Give me a name then!");
				System.out.print("> ");
				sc = new Scanner(System.in);
				String robotName = sc.nextLine(); 
				RobotName = robotName;
				haveName = !haveName;
				System.out.println("I have a name! I have a real name! I am the human interaction robot " + RobotName + " now. At your service!");
				return Action.DO_NOTHING;
			}else {
				System.out.println("I already have a name and I do not intend to change it. Ich bin "+ RobotName +". ");
				return Action.DO_NOTHING;
			}
		}

		return SearchForKeyword(dependencies, root);
	}


	public static Action SearchForKeyword(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Keyword Searched");
		if(originalString.contains("left")) {
			System.out.println(getRandom(KeywordResponses) + "go left");
			return Action.MOVE_LEFT;
		}else if(originalString.contains("right")) {
			System.out.println(getRandom(KeywordResponses) + "go right");
			return Action.MOVE_RIGHT;
		}else if(originalString.contains("up")) {
			System.out.println(getRandom(KeywordResponses) + "go up");
			return Action.MOVE_UP;
		}else if(originalString.contains("down")) {
			System.out.println(getRandom(KeywordResponses) + "go down");
			return Action.MOVE_DOWN;
		}else if(originalString.contains("clean")) {
			System.out.println(getRandom(KeywordResponses) + "clean the block");
			return Action.CLEAN;
		}else {
			List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

			String word = root.originalText().toLowerCase();
			if (word.equals("more") || 
					word.equals("further") || 
					word.equals("repeat") ||
					word.equals("again")){
				return readWordAction(word);
			}

			for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
				String prtWord = prt.second.originalText().toLowerCase();

				if (prtWord.equals("more") || 
						prtWord.equals("further") || 
						prtWord.equals("repeat") ||
						prtWord.equals("again")) {
					return readWordAction(word);
				}
			}


			System.out.println(getRandom(ClarificationResponses));
			return Action.DO_NOTHING;
		}

	}

	public static boolean checkNegate(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Negation Searched");
		String word = root.originalText().toLowerCase();

		switch (word) {
		case "no": return true;
		case "not": return true;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String prtWord = prt.second.originalText().toLowerCase();
			switch (prtWord) {
			case "no": return true;
			case "not": return true;
			}
		}

		return false;
	}

	public static boolean checkBye(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Bye Searched");
		String word = root.originalText().toLowerCase();

		switch (word) {
		case "bye": return true;
		case "goodbye": return true;
		case "cancel": return true;
		case "exit": return true;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String prtWord = prt.second.originalText().toLowerCase();
			switch (prtWord) {
			case "bye": return true;
			case "goodbye": return true;
			case "cancel": return true;
			case "exit": return true;
			}
		}

		return false;
	}

	public static boolean checkHi(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Greeting Searched");
		String word = root.originalText().toLowerCase();
		switch (word) {
		case "hi": return true;
		case "hello": return true;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String prtWord = prt.second.originalText().toLowerCase();
			switch (prtWord) {
			case "hi": return true;
			case "hello": return true;
			}
		}

		return false;
	}

	public static boolean checkName(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Name checked");
		String word = root.originalText().toLowerCase();
		switch (word) {
		case "name": return true;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String prtWord = prt.second.originalText().toLowerCase();
			switch (prtWord) {
			case "name": return true;
			}
		}

		return false;
	}

	public static boolean checkPraise(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Praise Searched");
		String word = root.originalText().toLowerCase();
		switch (word) {
		case "good": return true;
		case "well": return true;
		case "nice": return true;
		case "interesting": return true;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

		for (Pair<GrammaticalRelation, IndexedWord> prt : s) {
			String prtWord = prt.second.originalText().toLowerCase();
			switch (prtWord) {
			case "good": return true;
			case "well": return true;
			case "nice": return true;
			case "interesting": return true;
			}
		}

		return false;
	}
	
	
	// bfs
	public class State {
		
		public int row;
		public int column;
		public LinkedList<Action> path;
		public LinkedList<Position> targets;

		State(int row, int column, LinkedList currentA, LinkedList<Position> targets) {
			this.row = row;
			this.column = column;
			this.path = currentA;
			this.targets = targets;		
		}

	}
	
//	public void bfs() {
//		LinkedList<State> foundPath = new LinkedList<>();
//		LinkedList<State> visitedStates = new LinkedList<>();
//		State currentState = new State(this.posRow,this.posCol,new LinkedList<>(),this.env.getTargets());
//		foundPath.add(currentState);
//		this.openCount++;
//		while(!foundPath.isEmpty()) {
//			State currentPos = foundPath.remove();
//			if(!isVisited(currentPos.row,currentPos.column,currentPos,visitedStates)) {			
//				visitedStates.add(currentPos);
//				if(isInTargets(currentPos)) {
//					this.removeFromTargets(currentPos);
//					if(currentPos.targets.isEmpty()) {
//						this.pathFound = true;
//						if(this.pathLength == 0) {
//							this.path = currentPos.path;
//							this.pathLength = currentPos.path.size();
//							this.path.add(Action.DO_NOTHING);
//							return;
//						}else if(this.pathLength > currentPos.path.size()) {
//							this.path = currentPos.path;
//							this.pathLength = currentPos.path.size() - 1;
//						}
//					}else {
//					}
//				}
//				directionSearch(currentPos,visitedStates,foundPath);
//			}
//		}
//	}
	
//	public void directionSingleSearch(int row, int col, State currentPos,LinkedList<State> foundPath,Action act) {
//		LinkedList<Action> currentA = (LinkedList) currentPos.path.clone();
//		LinkedList<Position> currentT = (LinkedList) currentPos.targets.clone();
//		
//		currentA.add(act);
//		State nextState = new State(row,col,currentA,currentT);
//		foundPath.add(nextState);
//		this.openCount++;
//	}

	public boolean isVisited(int row,int col, State currentState, LinkedList<State> visited) {
		for(State currS:visited) {
			int targetNumber = 0;
			if(currS.row == row && currS.column == col) {
				for(Position target:currS.targets) {
					for(Position currT:currentState.targets) {
						if(target.row == currT.row && target.col == currT.col) {
							targetNumber++;
							if(targetNumber == currS.targets.size()) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
//	private void directionSearch(State currentPos, LinkedList<State> visitedStates, LinkedList<State> foundPath) {
//		if(this.isReadyForSearch(currentPos,visitedStates,foundPath,"up")) {
//			this.directionSingleSearch(currentPos.row-1, currentPos.column, currentPos, foundPath, Action.MOVE_UP);
//		}
//
//		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "down")) {
//			this.directionSingleSearch(currentPos.row+1, currentPos.column, currentPos, foundPath, Action.MOVE_DOWN);
//		}
//
//		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "left")) {
//			this.directionSingleSearch(currentPos.row, currentPos.column-1, currentPos, foundPath, Action.MOVE_LEFT);
//		}
//
//		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "right")) {
//			this.directionSingleSearch(currentPos.row, currentPos.column+1, currentPos, foundPath, Action.MOVE_RIGHT);
//		}
//		
//	}
	
	
	private void removeFromTargets(State currentPos) {
		Position currtarget = null;
		for(Position currT:currentPos.targets) {
			if(currT.row == currentPos.row && currT.col == currentPos.column) {
				currtarget = currT;
			}
		}
		currentPos.targets.remove(currtarget);		
	}
	
	public boolean isInTargets(State currentPos) {
		for(Position currT:currentPos.targets) {
			if(currT.row == currentPos.row && currT.col == currentPos.column) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isReadyForSearch(State currentPos, LinkedList<State> visitedStates, LinkedList<State> foundPath, String direction) {
		if(direction == "up") {
			return this.env.validPos(currentPos.row-1,currentPos.column) 
					&& !isVisited(currentPos.row-1,currentPos.column,currentPos,visitedStates)
					&& !isVisited(currentPos.row-1,currentPos.column,currentPos,foundPath);
		}else if(direction == "down") {
			return this.env.validPos(currentPos.row+1,currentPos.column) 
					&& !isVisited(currentPos.row+1,currentPos.column,currentPos,visitedStates)
					&& !isVisited(currentPos.row+1,currentPos.column,currentPos,foundPath);
		}else if(direction == "left") {
			return this.env.validPos(currentPos.row,currentPos.column-1) 
					&& !isVisited(currentPos.row,currentPos.column-1,currentPos,visitedStates)
					&& !isVisited(currentPos.row,currentPos.column-1,currentPos,foundPath);
		}else if(direction == "right") {
			return this.env.validPos(currentPos.row,currentPos.column+1) 
					&& !isVisited(currentPos.row,currentPos.column+1,currentPos,visitedStates)
					&& !isVisited(currentPos.row,currentPos.column+1,currentPos,foundPath);
		}else {
			return false;
		}
		
	}






}