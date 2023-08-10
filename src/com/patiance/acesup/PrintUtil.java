package com.patiance.acesup;

import java.util.List;

import com.patiance.card.Card;
import com.patiance.card.Deck;

public class PrintUtil {

	public static void printState(Deck deck, List<List<Card>> columns, boolean printDeck) {
		if (printDeck) {
			System.out.print("Deck: ");
			for (Card card : deck.getCards()) {
				String rank = card.getPrintableRank();
				String suit = card.getPrintableSuit();
				System.out.print(rank + suit + " ");
			}
			System.out.println();
		}
		for (int i = 0; i < columns.size(); ++i) {
			List<Card> cards = columns.get(i);
			for (Card card : cards) {
				String rank = card.getPrintableRank();
				String suit = card.getPrintableSuit();
				System.out.print(rank + suit + ",");
			}
			if (cards.size() == 0) {
				System.out.print("[ ]");
			}
			System.out.println();
		}
	}
}
