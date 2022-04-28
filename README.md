## scotlandyard

#Brief

The Scotland Yard Project is a piece of summative coursework set by COMS10017 submitted by 29/04/2022 for the development of Java application that allows for a digitally simulated playthrough of the popular board game "Scotland Yard": a game in which detectives are tasked with locating a criminal as they move around a board representing the streets of London. The aim of the project was to write a working Scotland Yard game model that passed all 82+ tests located in uk.ac.bris.cs.scotlandyard.model.AllTest and correctly simulates a playthrough of the game through the GUI located in the class uk.ac.bris.cs.scotlandyard.Main. The following report summarises the process of implementing a version of the core game component cw-model that achieves the aims of the project and reflects on Alexander Bloom's & Ivan Ho's achievements in understanding the principles of object orientation.


#Implementation

Our base code implements the Factory<GameState> interface through the MyGameStateFactory class with an inner class MyGameState that extends Board and thus implements 7 inherited methods and a further advance method, of which a new instance is initialised by the build method of the outer class MyGameStateFactory that calls the constructor of MyGameState. 


#Initialisation
  
The MyGameState constructor when called initialises a set of fields passed in by parameters with appropriate checks. Our getter methods also contain certain checks on their inputs.


#Getters
The task of the getters is to return the correct values based on the game state, most getters calculate and return needed values within the function however getMrXTravelLog, getWinner and getAvailableMoves rely on other  functions to calculate their returned values, these other functions are called and local attributes assigned a value in the constructor for  getWinner and getAvailable moves while for getMrXTravelLog it is  in the advance method.


#Available Moves
  
The task of the getAvailableMoves getter is to return all the available moves that all players have in the game as an ImmutableSet of Move objects, in order to return the correct value two helper functions makeSingleMoves and makeDoubleMoves are created that generate sets of objects representing all the possible moves of their respective type (single, double) that inputed Player can make . These functions are used in a loop located in the constructor that tallies all possible moves of all remaining players in game and is set to the local attribute moves that is returned by getAvailableMoves.


#Advance
  
The task of the advance method is to return a new state from the current GameState and provided Move. First inputed move is checked for validity. Then to extract information from the inputed move a new visitor class is created that extends the Move interface and gives us access to a particular SingleMove or DoubleMove by supplying a Visitor object of that type to the Move interface accept method, three classes are constructed, one to extract the used tickets from the inputed move, one to extract the destination from the move, and one return a new log entry from the destination and used tickets of move. This information and the newLog entry are then used to update the objects of the current GameState and return a new GameState in the advance method based on the rules of the game. 


#Determine Winner
  
The winner is determined in the constructor, through a series of checks for end game conditions that if met return a winner as a set of the winning players and if not return the winner as an empty set and recalculate the available moves of the remaining players. 


#Observer
  
A factory class MyModelFactory produces via build method a game Model that holds a GameState and an Observer list. An observer can be added and removed from the Observers list  and informed on the state of the game in regards to events MOVE_MADE and GAME_OVER through the chooseMove method.


#Conclusion
  
Following the implementation of the design outlined in this report all 82+ tests pass and the game is playable through the GUI, and the use and understanding of various OO concepts by Alexander Bloom and Ivan Ho has been demonstrated.
