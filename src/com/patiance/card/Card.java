package com.patiance.card;

public class Card {

	private int rank;

	private Suit suit;

	public Card(int rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public int getRank() {
		return rank;
	}

	public Suit getSuit() {
		return suit;
	}

	public String getPrintableRank() {
		switch (rank) {
			case 1:
			case 14:
				return "A";
			case 11:
				return "J";
			case 12:
				return "Q";
			case 13:
				return "K";
		}
		return rank + "";
	}

	public String getPrintableSuit() {
		switch (suit) {
			case DIAMONDS:
				return "♦";
			case CLUBS:
				return "♣";
			case HEARTS:
				return "♥";
			case SPADES:
				return "♠";
		}
		throw new RuntimeException("Missing suit");
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
