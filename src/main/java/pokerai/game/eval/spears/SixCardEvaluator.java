package pokerai.game.eval.spears;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class SixCardEvaluator implements HandEvaluatorSpears {
	private static FiveCardEvaluator fiveCardEvaluator = new FiveCardEvaluator();
	private static HashMap<Long, Integer> ranks;
	private static final int[][] choose5From6 = new int[][] {
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 5},
			{0, 1, 2, 4, 5},
			{0, 1, 3, 4, 5},
			{0, 2, 3, 4, 5},
			{1, 2, 3, 4, 5}
	};
	private static Card[][] suitedHands = new Card[4][6];
	private static int[] lengths = new int[4];
	private static final String ranksPath = ".\\pokerai\\game\\eval\\spears\\ranks";
	
	@SuppressWarnings("unchecked")
	public SixCardEvaluator()  {
		try {
			FileInputStream fis = new FileInputStream(ranksPath);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ranks = (HashMap<Long, Integer>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	public int evaluate(Card[] hand)   {
		long product = 1;
		for (int i = 0; i < 6; i++)  {
			int prime = FiveCardEvaluator.primes[hand[i].rank.ordinal()];
			product *= prime;
		}
    //System.out.println(ranks);

    int rank = ranks.get(product);
		
		// Look for flushes
		for (int i = 0; i < 4; i++) lengths[i] = 0;
		int flushSuit = -1;
		for (int i = 0; i < 6; i++) {
			Card card = hand[i];
			int suit = card.suit.ordinal();
			suitedHands[suit][lengths[suit]] = card;
			lengths[suit]++;
			if(lengths[suit] > 4) flushSuit = suit;
		}
		
		// No flushes found
		if(flushSuit == -1) return rank;
		
		// Flushes found
		int flushRank = Integer.MAX_VALUE;
		if(lengths[flushSuit] == 5){
			flushRank = fiveCardEvaluator.evaluate(suitedHands[flushSuit]);
			return Math.min(rank, flushRank);
		}
		
		// 6 card flush found
		for (int[] c56 : choose5From6) {
			flushRank = fiveCardEvaluator.evaluate(hand[c56[0]],hand[c56[1]],hand[c56[2]],hand[c56[3]],hand[c56[4]]);
			rank = Math.min(rank, flushRank);	
		}
		return rank;
	}
	
		
	@SuppressWarnings("unused")
	private static void generate(String path) throws Exception {
		HashMap<Long, Integer> ranks = new HashMap<Long, Integer>();
		int[] i = new int[6];
		Card[] hand = new Card[5];
		int[] rankCounts = new int[13];
		
		FiveCardEvaluator evaluator = new FiveCardEvaluator();
		
		for (i[0] = 0; i[0] < 13; i[0]++) {
			for (i[1] = 0; i[1] < 13; i[1]++) {
				for (i[2] = 0; i[2] < 13; i[2]++) {
					for (i[3] = 0; i[3] < 13; i[3]++) {
						for (i[4] = 0; i[4] < 13; i[4]++) {
					  ranks: for (i[5] = 0; i[5] < 13; i[5]++) {
						  
								for (int c = 0; c < 6; c++) {
									System.out.print(i[c] + " ");
								}
								System.out.println();
								
								for(int r = 0; r < 13; r++) {
									rankCounts[r] = 0;
								}
								
								long product = 1;
								for (int c = 0; c < 6; c++) {
									Rank rank = Rank.values()[i[c]];
									rankCounts[i[c]]++;
									int prime = FiveCardEvaluator.primes[rank.ordinal()];
									product *= prime;
									if(product < 0) throw new Exception("overflow");	
								}
								if(ranks.containsKey(product)) continue ranks;
								
								for(int r = 0; r < 13; r++) {
									if(rankCounts[r] > 4) {
										continue ranks;
									};
								}
								
								int minRank = Integer.MAX_VALUE;
								for (int[] c56 : choose5From6) {
									int j = 0;
									for (int c : c56) {
										Rank rank = Rank.values()[i[c]];
										Suit suit = Suit.values()[j % 4];
										hand[j] = Card.get(rank, suit);
										j++;
									}
									int rank = evaluator.evaluate(hand);
									minRank = Math.min(minRank, rank);
								}
								ranks.put(product, minRank);
							}	
						}	
					}	
				}	
			}
		}
		
		FileOutputStream file = new FileOutputStream(path);
		ObjectOutputStream object = new ObjectOutputStream(file);
		object.writeObject(ranks);
		file.close();
	}
	
	public static void main(String[] args) throws Exception {
		generate(ranksPath);
	}
	


}
