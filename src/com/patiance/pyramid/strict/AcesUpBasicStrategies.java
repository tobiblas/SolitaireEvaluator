package com.patiance.pyramid.strict;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.patiance.card.Card;
import com.patiance.card.Deck;

public class AcesUpBasicStrategies {

	enum Strategy {
		NO_MOVE, // never move a card to empty columns
		RANDOM, // move a random card to empty columns
		RANDOM_PRIORITIZE_ACES, //move ace if possible, else move random card
		PRIORITIZE_HIGH_CARDS, //if more than one card can be moved prioritize the highest
	}

	public static void main(String[] args) {
		for (Strategy strategy : Strategy.values()) {
			System.out.println("## Strategy " + strategy.name() + " ##");
			long gamesCount = 0;
			long successCount = 0;
			for (; gamesCount < 10000000l; ++gamesCount) {
				boolean success = playGame(strategy);
				if (success) {
					successCount++;
				}
			}
			System.out.println("games: " + gamesCount);
			System.out.println("success rate: " + (successCount / (float) gamesCount));
			System.out.println();
		}
	}

	private static boolean playGame(Strategy strategy) {
		Deck deck = Deck.randomStandardDeckAcesHigh();
		List<List<Card>> columns = new ArrayList(4);
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		columns.add(new ArrayList<>());
		return gameLoop(strategy, deck, columns);
	}

	private static boolean gameLoop(Strategy strategy, Deck deck, List<List<Card>> columns) {
		while (!deck.isEmpty()) {
			columns.get(0).add(deck.pickCard());
			columns.get(1).add(deck.pickCard());
			columns.get(2).add(deck.pickCard());
			columns.get(3).add(deck.pickCard());
			while (true) {
				boolean removed = removeCards(columns);
				if (!removed) {
					break;
				}
				if (!canCardsBeMoved(columns)) {
					break;
				} else {
					moveCards(columns, strategy);
				}
			}
		}
		return gameCompleted(columns);
	}

	private static void moveCards(List<List<Card>> columns, Strategy strategy) {
		if (strategy == Strategy.NO_MOVE) {
			return;
		}
		if (strategy == Strategy.PRIORITIZE_HIGH_CARDS) {
			List<Integer> indexesToMoveTo = new ArrayList<>();
			List<Integer> indexesToMoveFrom = new ArrayList<>();
			boolean moved = true;
			while (moved) {
				getIndexes(indexesToMoveFrom, indexesToMoveTo, columns);
				moved = moveOneCard(columns, indexesToMoveTo, indexesToMoveFrom);
			}
		}
		if (strategy == Strategy.RANDOM_PRIORITIZE_ACES) {
			Random random = new Random(System.currentTimeMillis());
			int index = 0;
			for (List<Card> column : columns) {
				if (column.isEmpty()) {
					List<Integer> indexesToMoveFrom = new ArrayList<>();
					for (int i = 0; i < 4; i++) {
						if (i == index) {
							continue;
						}
						List<Card> colWithMaybeAce = columns.get(i);
						if (colWithMaybeAce.size() > 1 && colWithMaybeAce.get(colWithMaybeAce.size() - 1).getRank() == 14) {
							indexesToMoveFrom.add(i);
						}
					}
					if (indexesToMoveFrom.size() == 0) {
						break;
					}
					int pickFrom = random.nextInt(indexesToMoveFrom.size());
					pickFrom = indexesToMoveFrom.get(pickFrom);
					List<Card> cardsToTakeFrom = columns.get(pickFrom);
					Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
					columns.get(index).add(card);
				}
				index++;
			}
		}
		if (strategy == Strategy.RANDOM || strategy == Strategy.RANDOM_PRIORITIZE_ACES) {
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
						return;
					}
					int pickFrom = random.nextInt(indexesToMoveFrom.size());
					pickFrom = indexesToMoveFrom.get(pickFrom);
					List<Card> cardsToTakeFrom = columns.get(pickFrom);
					Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
					columns.get(index).add(card);
				}
				index++;
			}
		}
	}

	private static boolean moveOneCard(List<List<Card>> columns, List<Integer> indexesToMoveTo, List<Integer> indexesToMoveFrom) {
		int highestRankIsOnIndex = -1;
		int highest = -1;
		if (indexesToMoveFrom.size() < 1 || indexesToMoveTo.size() < 1) {
			return false;
		}
		for (Integer fromIndex : indexesToMoveFrom) {
			List<Card> col = columns.get(fromIndex);
			int rank = col.get(col.size() - 1).getRank();
			if (rank > highest) {
				highest = rank;
				highestRankIsOnIndex = fromIndex;
			}
		}
		List<Card> cardsToTakeFrom = columns.get(highestRankIsOnIndex);
		Card card = cardsToTakeFrom.remove(cardsToTakeFrom.size() - 1);
		columns.get(indexesToMoveTo.get(0)).add(card);
		return true;
	}

	private static void getIndexes(List<Integer> indexesToMoveFrom, List<Integer> indexesToMoveTo, List<List<Card>> columns) {
		indexesToMoveFrom.clear();
		indexesToMoveTo.clear();
		for (int i = 0; i < 4; ++i) {
			List<Card> column = columns.get(i);
			if (column.isEmpty()) {
				indexesToMoveTo.add(i);
			} else if (column.size() > 1) {
				indexesToMoveFrom.add(i);
			}
		}
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
