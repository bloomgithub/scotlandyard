package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

// START GAMESTATE
public final class MyGameStateFactory implements Factory<GameState> {
	// START CONSTRUCTOR

	private final class MyGameState implements GameState {

		// START ATTRIBUTES

		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		// END ATTRIBUTES

		// START INITIALISATION

		private MyGameState(

				// Initial parameters
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){

				// START CHECKS

				// Check if Mrx is null
				if (Objects.isNull(mrX))
					throw new NullPointerException("Mr X is null!");

				// Check if detectives is null
				if (Objects.isNull(detectives))
					throw new NullPointerException("Detectives is null!");

				// Check if Mrx exists
				if (!mrX.piece().isMrX())
					throw new IllegalArgumentException("No mrX!");

				// Check if there are more than one Mrx or if detectives and mrX have been swapped
				for (Player detective : detectives)
					if (detective.piece().isMrX())
						throw new IllegalArgumentException("More than one Mr X or swapped Mr X!");

				// Check if there are more than one of the same detective
				final Set<Player> detectiveHashSet = new HashSet<>();
				for (Player detective : detectives)
					if (!detectiveHashSet.add(detective))
						throw new IllegalArgumentException("Duplicate detective!");

				// Check if there are more than one of the same detective
				final Set<Integer> detectiveLocationHashSet = new HashSet<>();
				for (Player detective : detectives)
					if (!detectiveLocationHashSet.add(detective.location()))
						throw new IllegalArgumentException("Detective location overlap!");

				// Check if the detectives have the secret ticket
				for (Player detective : detectives)
					if (detective.tickets().getOrDefault(Ticket.SECRET,0) != 0)
						throw new IllegalArgumentException("Detectives have the secret ticket!");

				// Check if the detectives have the double ticket
				for (Player detective : detectives)
					if (detective.tickets().getOrDefault(Ticket.DOUBLE,0) != 0)
						throw new IllegalArgumentException("Detectives have the double ticket!");

				// Check if moves is empty
				if(setup.moves.isEmpty())
					throw new IllegalArgumentException("Empty moves!");

				// Check if graph is empty
				if (setup.graph.nodes().isEmpty())
					throw new IllegalArgumentException("Empty graph!");

				// END CHECKS

				// END INITIALISATION

				// Initialised the local attributes
				this.setup      = setup;
				this.remaining  = remaining;
				this.log        = log;
				this.mrX        = mrX;
				this.detectives = detectives;

				// START DETERMINE WINNER

				// Store all detective pieces for return winners after end game conditions check
				ArrayList<Piece> allDetectives = (ArrayList<Piece>) detectives.stream().filter(Player::isDetective).map(Player::piece).collect(Collectors.toList());

				// The detectives win, if:
				// A detective finish a move on the same station as Mr X.
				if(detectives.stream().anyMatch(p -> p.location() == mrX.location())){

					winner =  ImmutableSet.copyOf(allDetectives);
					moves  = ImmutableSet.of();
				}
				// There are no unoccupied stations for Mr X to travel to.
				else if(movesOf(List.of(mrX)).isEmpty() && remaining.contains(MrX.MRX)){

					winner = ImmutableSet.copyOf(allDetectives);
					moves  = ImmutableSet.of();
				}
				// Mr X wins, if:
				// Mr X manages to fill the log and the detectives subsequently fail to catch him with their final moves.
				else if(movesOf(detectives).isEmpty() ) {

					winner = ImmutableSet.of(MrX.MRX);
					moves  = ImmutableSet.of();
				}
				// The detectives can no longer move any of their playing pieces.
				else if(remaining.contains(MrX.MRX) && log.size() == setup.moves.size()) {

					winner = ImmutableSet.of(MrX.MRX);
					moves  = ImmutableSet.of();
				}
				// End game conditions not met
				else{

					// No winner returned
					winner = ImmutableSet.of();
					// No winner returned

					// Store of SingleMove and DoubleMove generated
					final var allMoves = new ArrayList<Move>();

					// Generate SingleMove and DounbleMove of remaining players
					for(Piece temp : remaining) {
						Player player = playerFromPiece(temp);
						allMoves.addAll(makeSingleMoves(setup, detectives,player, player.location()));
						allMoves.addAll(makeDoubleMoves(setup, detectives,player, player.location()));
					}

					// Return available moves of remaining players
					moves = ImmutableSet.copyOf(allMoves);
				}
				// END DETERMINE WINNER
		}
		// END CONSTRUCTOR

		// START GETTERS

		// Return the current game setup
		@Override
		public GameSetup getSetup() { return setup; }

		// Return all players in the game
		@Override
		public ImmutableSet<Piece> getPlayers() {

			Set<Piece> players = new HashSet<>();
			for (Player detective : detectives)
				players.add(detective.piece());
			players.add(mrX.piece());
			ImmutableSet<Piece> immutablePlayer = ImmutableSet.copyOf(players);
			return immutablePlayer;
		}

		// Return the location of the given detective; empty if the detective is not part of the game
		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {

			// For all detectives, if Detective#piece == detective, then return the location in an Optional.of();
			Player foundDetective = null;
			for (Player findingDetective : detectives)
				if (findingDetective.piece() == detective)
					foundDetective = findingDetective;

			if (foundDetective != null)
				return Optional.of(foundDetective.location());
			else
				// Otherwise, return Optional.empty();
				return Optional.empty();
		}

		// Return the ticket board of the given player; empty if the player is not part of the game
		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			// Store player for TicketBoard check
			Player foundPlayer = null;
			// Check if piece Mr X
			if (mrX.piece() == piece)
				foundPlayer = mrX;
			else
				// Check if piece is a detective
				for (Player detective : detectives)
					if (detective.piece() == piece)
						foundPlayer = detective;

			Player finalFoundPiece = foundPlayer;

			// Inner class for TickerBoard
			TicketBoard ticketBoard = new TicketBoard() {
				// Return tickets of stored player
				@Override
				public int getCount(@Nonnull Ticket ticket) {
					return finalFoundPiece.tickets().getOrDefault(ticket, 0);
				}
			};

			// If player exists return its TickerBoard
			if (foundPlayer != null)
				return Optional.of(ticketBoard);
			else
				return Optional.empty();
		}

		// Return Mr X travel log
		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() { return log; }

		// START ADVANCE

		@Nonnull
		@Override
		public GameState advance(Move move) {

			// Check is that the provided move is indeed valid
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move); // check if the move is possible or not

			// Store current mrX
			Player newMrX = mrX;

			// Store shared new location for Mr X and detectives
			int newLocation = locationAfterMove(move);

			// START STATE UPDATE
			// If it's Mr X's turn
			if(move.commencedBy().isMrX()) {

				// Store existing log for MyGameState
				var newLog = new ArrayList<LogEntry>(log);

				// Get detective pieces for MyGameState
				List<Piece> allDetectives = detectives.stream().filter(p -> p.isDetective()).map(p -> p.piece()).collect(Collectors.toList());

				// Add their move(s) to the log
				newLog.addAll(logEntriesAfterMove(setup, log, move));

				//Take the used ticket(s) away from Mr X
				newMrX = newMrX.use(ticketsUsedAfterMove(move));

				// Move Mr X's position to their new destination
				newMrX = newMrX.at(newLocation);

				// Swap to the detectives turn
				// Advancement of the GameState
				return new MyGameState(setup, ImmutableSet.copyOf(allDetectives), ImmutableList.copyOf(newLog), newMrX, detectives);
			}
			// If it's the detectives' turn
			else {

				// Store detective who made move
				Player newDetective = playerFromPiece(move.commencedBy());

				// Store list of detectives who did not make a move
				Player finalNewDetective = newDetective;
				List<Player> newDetectives = detectives.stream().filter(detective -> detective != finalNewDetective).collect(Collectors.toList());

				// Move the detective to their new destination
				newDetective = newDetective.at(newLocation);

				// Take the used ticket from the detective and give it to Mr X
				Iterable<Ticket> usedTickets = move.tickets();
				newDetective = newDetective.use(usedTickets);
				newMrX = mrX.give(usedTickets);

				// Ensure that particular detective won't move again this round
				newDetectives.add(newDetective);

				// convert Piece list of remaining players to Player list remaining players to check for swap to Mr X turn
				List<Piece> newRemaining = remaining.stream().filter(p -> p != move.commencedBy()).collect(Collectors.toList());
				var remainingPlayers = new ArrayList<Player>();
				for(Piece piece : newRemaining)
					remainingPlayers.add(playerFromPiece(piece));

				// Advancement of the GameState
				if(movesOf(remainingPlayers).isEmpty())
					// If there are no more possible detective moves, swap to Mr X's turn
					return new MyGameState(setup, ImmutableSet.of(MrX.MRX), log, newMrX, newDetectives);
				else
					// Iterate through detective turns
					return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), log, newMrX, newDetectives);
			}
			// END STATE UPDATE
		}
		// END ADVANCE

		// Return winner of game
		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() { return winner; }

		// Return available moves in game
		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() { return moves; }

		// END GETTERS

		// Return Player from corresponding Piece
		private Player playerFromPiece(Piece piece){

			var allPlayers = new ArrayList<Player>(detectives);
			allPlayers.add(mrX);
			var immutableAllPlayers= ImmutableList.copyOf(allPlayers);
			return immutableAllPlayers.stream().filter(p -> p.piece() == piece).findFirst().get();
		}

		// Return available moves of corresponding list of players
		private ImmutableSet<Move> movesOf(List<Player> list){

			var allMoves = new ArrayList<Move>();

			for(Player player : list) {
				allMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				allMoves.addAll(makeDoubleMoves(setup, detectives,player,player.location()));
			}

			return ImmutableSet.copyOf(allMoves);
		}
	}
	// END CONSTRUCTOR

	// START AVAILABLE MOVES

	// Calculate set of all the possible single moves player can make
	private static ImmutableSet<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

		// Create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
		final var singleMoves = new ArrayList<Move.SingleMove>();

		for (int destination : setup.graph.adjacentNodes(source)) {
			boolean occupied = false;

			// Find out if destination is occupied by a detective
			for (Player detective : detectives)
				if (destination == detective.location())
				//  If the location is occupied, don't add to the collection of moves to return
				{
					occupied = true;
					break;
				}

			if (occupied) continue;

			for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
				// Find out if the player has the required tickets
				if (player.has(t.requiredTicket()))
					//  If it does, construct a SingleMove and add it the collection of moves to return
					singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
			}

			// Consider the rules of secret moves here
			if (player.has(Ticket.SECRET))
				// Add moves to the destination via a secret ticket if there are any left with the player
				singleMoves.add(new Move.SingleMove(player.piece(), source, Ticket.SECRET, destination)); // making sure to add the secret move if ticket for it is present
		}

		// Return the collection of moves
		return ImmutableSet.copyOf(singleMoves);
	}

	// Calculate set of all the possible double moves player can make
	private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

		final var singleMoves = makeSingleMoves(setup,detectives,player,source);

		// Create an empty collection of some sort, say, HashSet, to store all the DoubleMove we generate
		final var doubleMoves = new ArrayList<Move.DoubleMove>();

		// Check if player has required ticket and moves left
		if(player.has(Ticket.DOUBLE) && setup.moves.size() > 1) {

			for (Move.SingleMove move : singleMoves) {
				int firstDestination = move.destination;
				Ticket firstTicket = move.ticket;

				for (int secondDestination : setup.graph.adjacentNodes(firstDestination)) {
					boolean occupied = false;

					// Find out if second destination is occupied by a detective
					for (Player detective : detectives)
						if (secondDestination == detective.location())
						//  If the location is occupied, don't add to the collection of moves to return
						{
							occupied = true;
							break;
						}

					if (occupied) continue;

					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(firstDestination, secondDestination, ImmutableSet.of()))) {
						Ticket secondTicket = t.requiredTicket();

						// Find out if the player has the required tickets
						if (player.has(firstTicket) && player.has(secondTicket))
							if (firstTicket != secondTicket || player.hasAtLeast(firstTicket, 2))
								//  If it does, construct a DoubleMove and add it the collection of moves to return
								doubleMoves.add(new Move.DoubleMove(player.piece(), source, firstTicket, firstDestination, secondTicket, secondDestination));
					}

					// consider the rules of secret moves
					if (player.has(Ticket.SECRET))
						//  Add moves to the destination via a secret ticket if there are any left with the player
						doubleMoves.add(new Move.DoubleMove(player.piece(), source, firstTicket, firstDestination, Ticket.SECRET, secondDestination));
				}
			}
		}
		// Return the collection of moves
		return ImmutableSet.copyOf(doubleMoves);
	}
	// END AVAILABLE MOVES

	// START VISITOR PATTERN

	// Returns location after game state advances
	private static int locationAfterMove(Move move){

		return move.accept(new Move.Visitor<Integer>() {
			@Override public Integer visit(Move.SingleMove singleMove){ return singleMove.destination; }
			@Override public Integer visit(Move.DoubleMove doubleMove){ return doubleMove.destination2; }
		});
	}

	// Returns used tickets from last game state when game state advances
	private static Iterable<Ticket> ticketsUsedAfterMove(Move move){

		return move.accept(new Move.Visitor<Iterable<Ticket>>() {
			@Override public Iterable<Ticket> visit(Move.SingleMove singleMove) { return singleMove.tickets(); }
			@Override public Iterable<Ticket> visit(Move.DoubleMove doubleMove) { return doubleMove.tickets(); }
		});
	}

	// Returns updated Mr X log when game state advances
	private static List<LogEntry> logEntriesAfterMove(GameSetup setup, ImmutableList<LogEntry> log, Move move){

		// If a move should be revealed according to the GameSetup, reveal the destination in the log, otherwise keep the desination hidden
		return move.accept(new Move.Visitor<>() {
			@Override
			public List<LogEntry> visit(Move.SingleMove singleMove) {
				if (setup.moves.get(log.size()))
					return List.of(LogEntry.reveal(singleMove.ticket, singleMove.destination));
				else
					return List.of(LogEntry.hidden(singleMove.ticket));
			}
			@Override
			public List<LogEntry> visit(Move.DoubleMove doubleMove) {
				var newLog = new ArrayList<LogEntry>();

				if (setup.moves.get(log.size()))
					newLog.add(LogEntry.reveal(doubleMove.ticket1, doubleMove.destination1));
				else
					newLog.add(LogEntry.hidden(doubleMove.ticket1));

				if (setup.moves.get(log.size() + 1))
					newLog.add(LogEntry.reveal(doubleMove.ticket2, doubleMove.destination2));
				else
					newLog.add(LogEntry.hidden(doubleMove.ticket2));

				return newLog;
			}
		});
	}
	// END VISITOR PATTERN

	// Build method of MyGameStateFactory
	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives){
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}
}
// END GAME STATE