package com.patiance.acesup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.patiance.card.Card;
import com.patiance.card.Deck;

/**
 * Whenever a decision has to be made evaluate all possible moves with the evaluation function
 * and choose the best move.
 */
public class AcesUpEvalFunction {

	public static Evaluator evaluator = new Evaluator();

	public static void main(String[] args) {
		long gamesCount = 0;
		long successCount = 0;
		for (; gamesCount < 10000000; ++gamesCount) {
			boolean success = playGame();
			if (success) {
				successCount++;
			}
			if (gamesCount % 1000000 == 0) {
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				String formattedTimestamp = now.format(formatter);
				System.out.println(formattedTimestamp + " games: " + (gamesCount + 1) + " success: " + successCount);
			}
		}
		System.out.println("games: " + gamesCount);
		System.out.println("success: " + successCount);
		System.out.println("success rate: " + (successCount / (float) gamesCount));
	}

	private static boolean playGame() {
		Deck deck = Deck.randomStandardDeckAcesHigh();
		List<List<Card>> columns = new ArrayList(4);
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		return gameLoop(deck, columns);
	}

	private static boolean gameLoop(Deck deck, List<List<Card>> columns) {
		while (!deck.isEmpty()) {
			pickCard(deck, columns);
			if (!canBeCompleted(deck, columns)) {
				return false;
			}
			removeCards(columns);
			if (canCardsBeMoved(columns)) {
				Map<List<Move>, Integer> scores = new HashMap<>();
				getBestMoves(deck, scores, new ArrayList<>(), columns);
				List<Move> bestMoves = getBest(scores);
				doMoves(bestMoves, columns);
			}
		}
		return gameCompleted(columns);
	}

	private static void doMoves(List<Move> moves, List<List<Card>> columns) {
		for (Move move : moves) {
			List<Card> cardsToTakeFrom = columns.get(move.from);
			Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
			columns.get(move.to).add(card);
			removeCards(columns);
		}
	}

	private static List<Move> getBest(Map<List<Move>, Integer> scores) {
		List<Move> best = null;
		int bestScore = Integer.MIN_VALUE;
		for (Entry<List<AcesUpEvalFunction.Move>, Integer> entry : scores.entrySet()) {
			int score = entry.getValue();
			if (score >= bestScore) {
				bestScore = score;
				best = entry.getKey();
			}
		}
		return best;
	}

	private static void pickCard(Deck deck, List<List<Card>> columns) {
		columns.get(0).add(deck.pickCard());
		columns.get(1).add(deck.pickCard());
		columns.get(2).add(deck.pickCard());
		columns.get(3).add(deck.pickCard());
	}

	private static void getBestMoves(Deck deck, Map<List<Move>, Integer> scores, List<Move> moves, List<List<Card>> columns) {
		while (true) {
			removeCards(columns);
			if (!canCardsBeMoved(columns)) {
				scores.put(moves, evaluator.evaluateState(deck, columns));
				return;
			} else {
				int possibleMoves = possibleMoves(columns);
				for (int i = 0; i < possibleMoves; ++i) {
					List<List<Card>> columnsClone = clone(columns);
					Move move = doMove(columnsClone, i);
					//PrintUtil.printState(deck, columns, true);
					List<Move> movesClone = cloneMoves(moves);
					movesClone.add(move);
					getBestMoves(deck, scores, movesClone, columnsClone);
				}
				return;
			}
		}
	}

	private static List<Move> cloneMoves(List<Move> moves) {
		List<Move> clone = new ArrayList<>();
		for (Move move : moves) {
			Move move2 = new Move();
			move2.from = move.from;
			move2.to = move.to;
			move2.nomove = move.nomove;
			clone.add(move2);
		}
		return clone;
	}

	private static class Move {

		boolean nomove;
		int from;
		int to;
	}

	private static Move doMove(List<List<Card>> columns, int index) {
		List<Move> allMoves = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			List<Card> column = columns.get(i);
			if (column.size() == 0) {
				for (int j = 0; j < 4; j++) {
					if (j == i) {
						continue;
					}
					if (columns.get(j).size() > 1) {
						Move move = new Move();
						move.from = j;
						move.to = i;
						allMoves.add(move);
					}
				}
			}
		}
		if (index >= allMoves.size()) {
			Move move = new Move();
			move.nomove = true;
			return move; // do the no move
		}
		Move move = allMoves.get(index);
		List<Card> cardsToTakeFrom = columns.get(move.from);
		Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
		columns.get(move.to).add(card);
		return move;
	}

	private static List<List<Card>> clone(List<List<Card>> columns) {
		List<List<Card>> columnsClone = new ArrayList(4);
		columnsClone.add(new ArrayList<>());
		columnsClone.add(new ArrayList<>());
		columnsClone.add(new ArrayList<>());
		columnsClone.add(new ArrayList<>());
		for (int i = 0; i < 4; ++i) {
			for (Card card : columns.get(i)) {
				columnsClone.get(i).add(new Card(card.getRank(), card.getSuit()));
			}
		}
		return columnsClone;
	}

	private static int possibleMoves(List<List<Card>> columns) {
		int possibleMoves = 0;
		int colsWithMoreThanOneCard = (columns.get(0).size() > 1 ? 1 : 0) +
				(columns.get(1).size() > 1 ? 1 : 0) +
				(columns.get(2).size() > 1 ? 1 : 0) +
				(columns.get(3).size() > 1 ? 1 : 0);
		for (List<Card> column : columns) {
			if (column.size() > 0) {
				continue;
			}
			possibleMoves += colsWithMoreThanOneCard;
		}
		return possibleMoves;
	}

	private static boolean moveCards(List<List<Card>> columns) {
		boolean moved = false;
		Random random = new Random(System.currentTimeMillis());
		int index = 0;
		for (List<Card> column : columns) {
			if (column.size() == 0) {
				List<Integer> indexesToMoveFrom = new ArrayList<>();
				for (int i = 0; i < 4; i++) {
					if (i == index) {
						continue;
					}
					if (columns.get(i).size() > 1) {
						indexesToMoveFrom.add(i);
					}
				}
				if (indexesToMoveFrom.size() == 0) {
					return moved;
				}
				int pickFrom = random.nextInt(indexesToMoveFrom.size());
				pickFrom = indexesToMoveFrom.get(pickFrom);
				List<Card> cardsToTakeFrom = columns.get(pickFrom);
				Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
				columns.get(index).add(card);
				moved = true;
			}
			index++;
		}
		return moved;
	}

	private static boolean canBeCompleted(Deck deck, List<List<Card>> columns) {
		boolean allAcesOut = 0 == deck.getCards().stream().filter(card -> card.getRank() == 14).count();
		if (!allAcesOut) {
			return true;
		}
		int columnsWithoutAceInBottom = 0;
		for (List<Card> cards : columns) {
			boolean hasAce = false;
			for (Card card : cards) {
				if (card.getRank() == 14) {
					hasAce = true;
				}
			}
			if (!hasAce) {
				return true;
			}
			if (cards.size() > 0 && cards.get(0).getRank() != 14) {
				columnsWithoutAceInBottom++;
			}
		}
		if (columnsWithoutAceInBottom > 0) {
			return false;
		}
		return true;
	}

	private static boolean canCardsBeMoved(List<List<Card>> columns) {
		if (!isEmptyColumns(columns)) {
			return false;
		}
		return columns.get(0).size() > 1 ||
				columns.get(1).size() > 1 ||
				columns.get(2).size() > 1 ||
				columns.get(3).size() > 1;
	}

	private static boolean isEmptyColumns(List<List<Card>> columns) {
		return columns.get(0).isEmpty() ||
				columns.get(1).isEmpty() ||
				columns.get(2).isEmpty() ||
				columns.get(3).isEmpty();
	}

	private static boolean gameCompleted(List<List<Card>> columns) {
		boolean completed = columns.get(0).size() == 1 &&
				columns.get(1).size() == 1 &&
				columns.get(2).size() == 1 &&
				columns.get(3).size() == 1;
		return completed;
	}

	private static boolean removeCards(List<List<Card>> columns) {
		int previousState = -1;
		int newState = 0;
		boolean removed = false;
		while (previousState != newState) {
			previousState = columns.get(0).size() + columns.get(1).size() + columns.get(2).size() + columns.get(3).size();
			for (int currentColumn = 0; currentColumn < 4; ++currentColumn) {
				Card currentCard = getTopCard(columns, currentColumn);
				//can this card be removed?
				for (int i = 0; i < 4; ++i) {
					Card otherCard = getTopCard(columns, i);
					if (otherCard == currentCard || otherCard == null || currentCard == null) {
						continue;
					}
					if (canCardRemoveCard(otherCard, currentCard)) {
						removeCard(columns, currentColumn);
						removed = true;
						break;
					}
				}
			}
			newState = columns.get(0).size() + columns.get(1).size() + columns.get(2).size() + columns.get(3).size();
		}
		return removed;
	}

	private static void removeCard(List<List<Card>> columns, int index) {
		List<Card> column = columns.get(index);
		if (column.size() == 0) {
			return;
		}
		column.remove(column.size() - 1);
	}

	private static boolean canCardRemoveCard(Card otherCard, Card currentCard) {
		if (otherCard.getSuit() != currentCard.getSuit()) {
			return false;
		}
		return otherCard.getRank() > currentCard.getRank();
	}

	private static Card getTopCard(List<List<Card>> columns, int index) {
		List<Card> column = columns.get(index);
		if (column.size() == 0) {
			return null;
		}
		return column.get(column.size() - 1);
	}
}
