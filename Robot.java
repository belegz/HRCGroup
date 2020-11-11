import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


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
	private boolean toCleanOrNotToClean;
	private ArrayList<Position> dirtyTiles = new ArrayList<>();
	private boolean isFirst = true;
	private ArrayList<Action> path = new ArrayList<>();
	private boolean pathFound = false;
	private int pathLength;
	private boolean isFinding = false;
	private ArrayList<Action> plan = new ArrayList<>();
	private static ArrayList<Position> remainingDirtyTiles = new ArrayList<>();
	private static boolean isFirstRobot = true;
	
	private static int order = 0;
	private int myOrder;
	private static Map<Integer,Boolean> RobotList = new HashMap<>();
	private static Map<Integer,Position> currentTargets = new HashMap<>();
	
	private static Map<Integer,Position> nextPositions = new HashMap<>();
	
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	 */


	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		order++;
		this.myOrder = order;
		this.RobotList.put(this.myOrder, false);
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
	public Action getMatrix() {
		double[][] matrix = new double[this.env.getRows()][this.env.getCols()];
		
		Tile[][] map = this.env.getTiles();
		for(int i = 0;i<this.env.getRows();i++) {
			for(int j= 0;j<this.env.getCols();j++) {
				if(map[i][j].getStatus() == TileStatus.DIRTY) {
					matrix[i][j] = 1;
				}else if(map[i][j].getStatus() == TileStatus.CLEAN) {
					matrix[i][j] = -0.4;
				}else if(map[i][j].getStatus() == TileStatus.IMPASSABLE) {
					//TODO: determine wall's value
					matrix[i][j] = 0;
				}
			}
		}
		
		//TODO: find next action with argmax
		//action up
		//moving up successfully
		for(int i = 0;i<this.env.getRows();i++) {
			for(int j= 0;j<this.env.getCols();j++) {
				if(map[i][j].getStatus() == TileStatus.DIRTY) {
					matrix[i][j] = 1;
				}else if(map[i][j].getStatus() == TileStatus.CLEAN) {
					matrix[i][j] = -0.4;
				}else if(map[i][j].getStatus() == TileStatus.IMPASSABLE) {
					//TODO: determine wall's value
					matrix[i][j] = 0;
				}
			}
		}
		return Action.DO_NOTHING;
	}
	public Action getAction() {

//		// Part 1
//		this.dirtyTiles = getDirtyTiles();
//		if(this.dirtyTiles.size() == 0) {
//			return Action.DO_NOTHING;
//		}
//		int mindis_x = this.env.getCols();
//		int mindis_y = this.env.getRows();
//		int mindis = mindis_x + mindis_y;
//		for(Position target:this.dirtyTiles) {
//			int new_x = target.col - this.getPosCol();
//			int new_y = target.row - this.getPosRow();
//			if(new_x == 0 && new_y == 0) {
//				System.out.println("Cleaned pos: "+target.col+","+target.row);
//				return Action.CLEAN;
//			}
//			int new_dis = Math.abs(new_x )+ Math.abs(new_y);
//			if( new_dis < mindis) {
//				mindis = new_dis;
//				mindis_x = new_x;
//				mindis_y = new_y;
//			}
//		}
//		if(mindis_x == 0) {
//			if(mindis_y > 0) {
//				return Action.MOVE_DOWN;
//			}else{
//				return Action.MOVE_UP;
//			}
//		}else if(mindis_y == 0) {
//			if(mindis_x > 0) {
//				return Action.MOVE_RIGHT;
//			}else{
//				return Action.MOVE_LEFT;
//			}
//		}
//
//		if(mindis_x <= mindis_y) {
//			// move on x
//			if(mindis_x > 0) {
//				return Action.MOVE_RIGHT;
//			}else{
//				return Action.MOVE_LEFT;
//			}
//		}else if(mindis_x > mindis_y) {
//			// move on y
//			if(mindis_y > 0) {
//				return Action.MOVE_DOWN;
//			}else{
//				return Action.MOVE_UP;
//			}
//		}
//		return Action.DO_NOTHING;

//		// Part 2: bfs search
//		if (this.env.getTiles()[this.posRow][this.posCol].getStatus() == TileStatus.DIRTY) {
//			return Action.CLEAN;
//		}
//
//		if(this.isFirst) {
//			bfs();
//			this.isFirst = false;			
//		}
//
//
//		if(this.plan.size() == 0) {
//			return Action.DO_NOTHING;
//		}
//
//		Action currentAction = this.plan.get(0);
//		this.plan.remove(0);
//		return currentAction;


//		// Part 3: communicate
//		if (this.env.getTiles()[this.posRow][this.posCol].getStatus() == TileStatus.DIRTY) {
//			return Action.CLEAN;
//		}
//		if(this.isFirstRobot) {
//			this.remainingDirtyTiles = this.getDirtyTiles();
//			this.isFirstRobot = false;
//		}
//		
//		if(this.remainingDirtyTiles.size()!=0 && this.plan.size() == 0) {
//			bfs();
//		}
//		if(this.plan.size() == 0) {
//			return Action.DO_NOTHING;
//		}
//
//		Action currentAction = this.plan.get(0);
//		this.plan.remove(0);
//		return currentAction;
		
//		// Part 4: remove target		
//		int currentCount = this.env.getRobots().size();	
//		if(currentCount < this.order) {	
//			for(Integer order:this.currentTargets.keySet()) {
//				boolean isContained = false;
//				for(Robot r:this.env.getRobots()) {
//					if(r.myOrder == order) {
//						isContained = true;
//						break;
//					}
//				}
//				
//				if(!isContained) {
//					Position toBeAdded = this.currentTargets.get(order);
//					this.remainingDirtyTiles.add(toBeAdded);
//					this.order = currentCount;
//					continue;
//				}
//			}
//		}
//
//		if (this.env.getTiles()[this.posRow][this.posCol].getStatus() == TileStatus.DIRTY) {
//			return Action.CLEAN;
//		}
//		if(this.isFirstRobot) {
//			this.remainingDirtyTiles = this.getDirtyTiles();
//			this.isFirstRobot = false;
//		}
//
//		if(this.remainingDirtyTiles.size()!=0 && this.plan.size() == 0) {
//			bfs();
//		}
//		if(this.plan.size() == 0) {
//			return Action.DO_NOTHING;
//		}
//
//		Action currentAction = this.plan.get(0);
//		this.plan.remove(0);
//		return currentAction;
		
		// Part 5: personal space		
		int currentCount = this.env.getRobots().size();	
		if(currentCount < this.order) {	
			for(Integer order:this.currentTargets.keySet()) {
				boolean isContained = false;
				for(Robot r:this.env.getRobots()) {
					if(r.myOrder == order) {
						isContained = true;
						break;
					}
				}
				
				if(!isContained) {
					Position toBeAdded = this.currentTargets.get(order);
					this.remainingDirtyTiles.add(toBeAdded);
					this.order = currentCount;
					continue;
				}
			}
		}

		if (this.env.getTiles()[this.posRow][this.posCol].getStatus() == TileStatus.DIRTY) {
			return Action.CLEAN;
		}
		if(this.isFirstRobot) {
			this.remainingDirtyTiles = this.getDirtyTiles();
			this.isFirstRobot = false;
		}

		if(this.remainingDirtyTiles.size()!=0 && this.plan.size() == 0) {
			bfs();
		}
		if(this.plan.size() == 0) {
			return Action.DO_NOTHING;
		}

		Action currentAction = this.plan.get(0);
		Position nextPos = null;
		boolean isInNext = false;
		if(currentAction.equals(Action.MOVE_UP)) {
			isInNext = this.checkInNextPos(this.posRow - 1, this.posCol);
			nextPos = new Position(this.posRow - 1,this.posCol);
		}else if(currentAction.equals(Action.MOVE_DOWN)) {
			isInNext = this.checkInNextPos(this.posRow + 1, this.posCol);
			nextPos = new Position(this.posRow + 1,this.posCol);
		}else if(currentAction.equals(Action.MOVE_LEFT)) {
			isInNext = this.checkInNextPos(this.posRow, this.posCol - 1);
			nextPos = new Position(this.posRow,this.posCol - 1);
		}else if(currentAction.equals(Action.MOVE_RIGHT)) {
			isInNext = this.checkInNextPos(this.posRow, this.posCol + 1);
			nextPos = new Position(this.posRow,this.posCol + 1);
		}
		
		if(isInNext) {
//			System.out.println("Is in!");
			bfs();
			if(this.plan.size() == 0) {
				return Action.DO_NOTHING;
			}

			Action nextAction = this.plan.remove(0);
			this.removeFromNextPos(this.posRow, this.posCol);
			return nextAction;
			
		}else {
			this.removeFromNextPos(this.posRow, this.posCol);
			this.nextPositions.put(this.myOrder, nextPos);
		}
		this.plan.remove(0);
		return currentAction;
	}

	public ArrayList<Position> getDirtyTiles() {
		ArrayList<Position> dirtyTiles = new ArrayList<>();
		for (int i = 0; i < this.env.getRows(); i++) {
			for (int j = 0; j < this.env.getCols(); j++) {
				Tile currentTile = this.env.getTiles()[i][j];
				if (currentTile.getStatus() == TileStatus.DIRTY) {
					Position dirtyPos = new Position(i,j);
					dirtyTiles.add(dirtyPos);
				}

			}
		}
		return dirtyTiles;
	}

	// bfs
	public class State {

		public int row;
		public int column;
		public ArrayList<Action> path;
		public ArrayList<Position> targets;

		State(int row, int column, ArrayList<Action> currentA, ArrayList<Position> currentT) {
			this.row = row;
			this.column = column;
			this.path = currentA;
			this.targets = currentT;		
		}

	}

	public void bfs() {
		ArrayList<State> foundPath = new ArrayList<>();
		ArrayList<State> visitedStates = new ArrayList<>();
		//		ArrayList<Position> destination =  getDirtyTiles();
		ArrayList<Position> destination =  this.remainingDirtyTiles;
		ArrayList<Action> minPath = new ArrayList<>();
		State currentState = new State(this.posRow,this.posCol,new ArrayList<>(),destination);
		foundPath.add(currentState);
		while(!foundPath.isEmpty()) {
			State currentPos = foundPath.remove(0);
			if(!isVisited(currentPos.row,currentPos.column,currentPos,visitedStates) && !this.checkInNextPos(currentPos.row, currentPos.column)) {			
				visitedStates.add(currentPos);
				if(isInTargets(currentPos)) {
					this.addToCurrent(currentPos.row, currentPos.column);
					this.removeFromDirtyTiles(currentPos.row, currentPos.column);
					this.removeFromTargets(currentPos);
					this.pathFound = true;
					minPath = currentPos.path;
					this.plan =  currentPos.path;
					this.pathLength = this.plan.size();
					return;
				}
				directionSearch(currentPos,visitedStates,foundPath);
			}
		}
	}

	public void directionSingleSearch(int row, int col, State currentPos,ArrayList<State> foundPath,Action act) {
		//			System.out.println("single search");
		ArrayList<Action> currentA = (ArrayList) currentPos.path.clone();
		ArrayList<Position> currentT = (ArrayList<Position>) currentPos.targets.clone();

		currentA.add(act);
		State nextState = new State(row,col,currentA,currentT);
		foundPath.add(nextState);
	}

	public boolean isVisited(int row,int col, State currentState, ArrayList<State> visitedStates) {
		for(State currS:visitedStates) {
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

	private void directionSearch(State currentPos, ArrayList<State> visitedStates, ArrayList<State> foundPath) {
		//			System.out.println("search");
		if(this.isReadyForSearch(currentPos,visitedStates,foundPath,"up")) {
			this.directionSingleSearch(currentPos.row-1, currentPos.column, currentPos, foundPath, Action.MOVE_UP);
		}

		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "down")) {
			this.directionSingleSearch(currentPos.row+1, currentPos.column, currentPos, foundPath, Action.MOVE_DOWN);
		}

		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "left")) {
			this.directionSingleSearch(currentPos.row, currentPos.column-1, currentPos, foundPath, Action.MOVE_LEFT);
		}

		if(this.isReadyForSearch(currentPos, visitedStates, foundPath, "right")) {
			this.directionSingleSearch(currentPos.row, currentPos.column+1, currentPos, foundPath, Action.MOVE_RIGHT);
		}

	}


	private void removeFromTargets(State currentPos) {
		Position currtarget = null;
		for(Position currT:currentPos.targets) {
			if(currT.row == currentPos.row && currT.col == currentPos.column) {
				currtarget = currT;
			}
		}
		currentPos.targets.remove(currtarget);		
	}



	private void removeFromDirtyTiles(int row, int col) {
		Position currtarget = null;
		for(Position currT:this.remainingDirtyTiles) {
			if(currT.row == row && currT.col == col) {
				currtarget = currT;
			}
		}
		this.remainingDirtyTiles.remove(currtarget);	
	}
	
	private boolean checkInNextPos(int row, int col) {
		for(int key:this.nextPositions.keySet()) {
			if(key != this.myOrder && 
					(this.nextPositions.get(key).row == row && this.nextPositions.get(key).col == col)) {
				return true;
			}
		}
		return false;
	}
	
	private void removeFromNextPos(int row, int col) {
		int toRemove = -1;
		for(int key:this.nextPositions.keySet()) {
			if(this.nextPositions.get(key).row == row && this.nextPositions.get(key).col == col) {
				toRemove = key;
				break;
			}
		}
//		System.out.println(this.nextPositions.toString());
		this.nextPositions.remove(toRemove);
//		System.out.println(this.nextPositions.toString());
	}
	
	private void addToCurrent(int row, int col) {
		Position currPos = null;
		for(Position currT:this.remainingDirtyTiles) {
			if(currT.row == row && currT.col == col) {
				currPos = currT;
			}
		}
		this.currentTargets.put(this.myOrder, currPos);
	}

	public boolean isInTargets(State currentPos) {
		for(Position currT:currentPos.targets) {
			if(currT.row == currentPos.row && currT.col == currentPos.column) {
				return true;
			}
		}
		return false;
	}

	private boolean isReadyForSearch(State currentPos, ArrayList<State> visitedStates, ArrayList<State> foundPath, String direction) {
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