package ca.jkmconsulting.crazyEightsCountdown.Enums;
public enum Card implements Comparable<Card> {
        CLUBS_2(Suit.CLUBS,CardRank.TWO,0),
        CLUBS_3(Suit.CLUBS,CardRank.THREE,1),
        CLUBS_4(Suit.CLUBS,CardRank.FOUR,2),
        CLUBS_5(Suit.CLUBS,CardRank.FIVE,3),
        CLUBS_6(Suit.CLUBS,CardRank.SIX,4),
        CLUBS_7(Suit.CLUBS,CardRank.SEVEN,5),
        CLUBS_8(Suit.CLUBS,CardRank.EIGHT,6),
        CLUBS_9(Suit.CLUBS,CardRank.NINE,7),
        CLUBS_10(Suit.CLUBS,CardRank.TEN,8),
        CLUBS_JACK(Suit.CLUBS,CardRank.JACK,9),
        CLUBS_QUEEN(Suit.CLUBS,CardRank.QUEEN,10),
        CLUBS_KING(Suit.CLUBS,CardRank.KING,11),
        CLUBS_ACE(Suit.CLUBS,CardRank.ACE,12),
        DIAMONDS_2(Suit.DIAMONDS,CardRank.TWO,13),
        DIAMONDS_3(Suit.DIAMONDS,CardRank.THREE,14),
        DIAMONDS_4(Suit.DIAMONDS,CardRank.FOUR,15),
        DIAMONDS_5(Suit.DIAMONDS,CardRank.FIVE,16),
        DIAMONDS_6(Suit.DIAMONDS,CardRank.SIX,17),
        DIAMONDS_7(Suit.DIAMONDS,CardRank.SEVEN,18),
        DIAMONDS_8(Suit.DIAMONDS,CardRank.EIGHT,19),
        DIAMONDS_9(Suit.DIAMONDS,CardRank.NINE,20),
        DIAMONDS_10(Suit.DIAMONDS,CardRank.TEN,21),
        DIAMONDS_JACK(Suit.DIAMONDS,CardRank.JACK,22),
        DIAMONDS_QUEEN(Suit.DIAMONDS,CardRank.QUEEN,23),
        DIAMONDS_KING(Suit.DIAMONDS,CardRank.KING,24),
        DIAMONDS_ACE(Suit.DIAMONDS,CardRank.ACE,25),
        HEARTS_2(Suit.HEARTS,CardRank.TWO,26),
        HEARTS_3(Suit.HEARTS,CardRank.THREE,27),
        HEARTS_4(Suit.HEARTS,CardRank.FOUR,28),
        HEARTS_5(Suit.HEARTS,CardRank.FIVE,29),
        HEARTS_6(Suit.HEARTS,CardRank.SIX,30),
        HEARTS_7(Suit.HEARTS,CardRank.SEVEN,31),
        HEARTS_8(Suit.HEARTS,CardRank.EIGHT,32),
        HEARTS_9(Suit.HEARTS,CardRank.NINE,33),
        HEARTS_10(Suit.HEARTS,CardRank.TEN,34),
        HEARTS_JACK(Suit.HEARTS,CardRank.JACK,35),
        HEARTS_QUEEN(Suit.HEARTS,CardRank.QUEEN,36),
        HEARTS_KING(Suit.HEARTS,CardRank.KING,37),
        HEARTS_ACE(Suit.HEARTS,CardRank.ACE,38),
        SPADES_2(Suit.SPADES,CardRank.TWO,39),
        SPADES_3(Suit.SPADES,CardRank.THREE,40),
        SPADES_4(Suit.SPADES,CardRank.FOUR,41),
        SPADES_5(Suit.SPADES,CardRank.FIVE,42),
        SPADES_6(Suit.SPADES,CardRank.SIX,43),
        SPADES_7(Suit.SPADES,CardRank.SEVEN,44),
        SPADES_8(Suit.SPADES,CardRank.EIGHT,45),
        SPADES_9(Suit.SPADES,CardRank.NINE,46),
        SPADES_10(Suit.SPADES,CardRank.TEN,47),
        SPADES_JACK(Suit.SPADES,CardRank.JACK,48),
        SPADES_QUEEN(Suit.SPADES,CardRank.QUEEN,49),
        SPADES_KING(Suit.SPADES,CardRank.KING,50),
        SPADES_ACE(Suit.SPADES,CardRank.ACE,51)
        ;
        public final Suit suit;
        public final CardRank rank;
        public final int index;

        Card(Suit suit, CardRank rank, int index) {
                this.suit = suit;
                this.rank = rank;
                this.index = index;
        }

        public Suit getSuit() {
                return suit;
        }

        public CardRank getRank() {
                return rank;
        }

        public int getIndex() {
                return index;
        }

        public boolean isWildCard() {
                return switch(this) {
                        case CLUBS_8 -> true;
                        case HEARTS_8 -> true;
                        case SPADES_8 -> true;
                        case DIAMONDS_8 -> true;
                        default -> false;
                };
        }
}
