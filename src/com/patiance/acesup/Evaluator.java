package com.patiance.acesup;

import java.util.List;

import com.patiance.card.Card;
import com.patiance.card.Deck;

public class Evaluator {

	public int evaluateState(Deck deck, List<List<Card>> columns) {
		int score = 0;
		if (!canBeCompleted(deck, columns)) {
			return Integer.MIN_VALUE;
		}
		score += getAceScore(columns);
		//all aces out and at least one not in bottom

		//empty column +1 - makes no difference
		//score += 1000 * getNumberOfEmptyColumns(columns);

		// minus number of cards on the table.  - makes no difference
		//score -= (columns.get(0).size() + columns.get(1).size() + columns.get(2).size() + columns.get(3).size());

		////better to have high card in bottom than lower.
		score += getBottomCardBonus(columns);
		return score;
	}

	private int getBottomCardBonus(List<List<Card>> columns) {
		int score = 0;
		for (List<Card> cards : columns) {
			if (cards.size() == 1) {
				score += cards.get(0).getRank();
			}
		}
		return score;
	}

	private int getNumberOfEmptyColumns(List<List<Card>> columns) {
		int count = 0;
		for (List<Card> cards : columns) {
			if (cards.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	//ace in the bottom: 2
	//ace not in bottom: -1
	private int getAceScore(List<List<Card>> columns) {
		int acesInBottom = 0;
		int acesNotInBottom = 0;

		for (List<Card> cards : columns) {
			int index = 0;
			for (Card card : cards) {
				if (card.getRank() == 14 && index == 0) {
					acesInBottom++;
				} else if (card.getRank() == 14 && index != 0) {
					acesNotInBottom++;
				}
				index++;
			}
		}
		return acesInBottom * 200 + acesNotInBottom * -100;
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
}
