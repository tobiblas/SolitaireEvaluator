package com.patiance;

import com.patiance.card.Deck;

public class Main {

	public static void main(String[] args) {
		Deck deck = Deck.randomStandardDeck();
		deck.print();
	}
}
