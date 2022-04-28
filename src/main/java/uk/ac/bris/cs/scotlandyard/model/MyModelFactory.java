package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Board.*;

import java.util.ArrayList;
import java.util.List;
public final class MyModelFactory implements Factory<Model> {
	// Implementation class of MyModel
	private final class MyModel implements Model{

		// First attributes
		private GameState currentState;
		private ImmutableSet<Observer> observers;

		// Constructor for MyModel
		private MyModel(
				// Incoming parameters
				final GameSetup setup,
				final Player MrX,
				final ImmutableList<Player> detectives) {

				// Initialised the local attributes
				this.currentState = new MyGameStateFactory().build(setup, MrX,detectives);
				this.observers    = ImmutableSet.of();
		}

		// Return list of observers
		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() { // getter method to access the set of observers
			return this.observers;
		}

		// Return current game state
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.currentState;
		}

		// Informs observers of state and event after game state advances
		@Override
		public void chooseMove(@Nonnull Move move) {
			// Advance the model with move
			this.currentState = currentState.advance(move);

			// Check if the game is over
			for(Observer observer : this.observers){
				// Inform the observers about the new state and event
				if(currentState.getWinner().isEmpty())
					observer.onModelChanged(getCurrentBoard(), Observer.Event.MOVE_MADE);
				else
					observer.onModelChanged(getCurrentBoard(), Observer.Event.GAME_OVER);
			}
		}

		// Add observer to observers list
		@Override
		public void registerObserver(@Nonnull Observer observer) {
			// Check if inputted observer registered twice
			for(Observer c : observers)
				if(c == observer) throw new IllegalArgumentException("Observer registers twice!");

			// Update observers in MyModel with registered observer
			List<Observer> newObservers = new ArrayList<>(observers);
			newObservers.add(observer);
			this.observers = ImmutableSet.copyOf(newObservers);
		}

		// Remove observer from observers list
		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			// Check if observer is null
			if(observer == null) throw new NullPointerException("Observer is null!");

			// Check if inputted observer is registered
			if (observers.stream().noneMatch(o -> o == observer))
				throw new IllegalArgumentException("Observer not registered!");

			// Update observers in MyModel removing unregistered observer
			List<Observer> newObservers = new ArrayList<>(observers);
			newObservers.remove(observer);
			this.observers = ImmutableSet.copyOf(newObservers);
		}

	}

	// Build method of MyModelFactory
	@Nonnull @Override public Model build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyModel(setup,mrX,detectives);
	}
}
