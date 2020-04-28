package LionShogi;
import java.util.HashMap;

import LionShogi.json.*;

/** Class handling game rules: possible moves and captures, win conditions, etc.*/
public class GameRules implements ShogiSelector
{
	// Array of piece types and information on them.
	private HashMap<Byte, char[][]> pieceMovementMap;
	private HashMap<Character, byte[]> pieceConversionTable;
	private HashMap<Byte, byte[]> piecePromotionMap;
	private HashMap<Byte, Integer> pieceValueMap;
	private HashMap<Byte, Character> showMap;
	private HashMap<Byte, Byte> demoteMap;

	/**
	* Getter for a piece as a byte array.
	* @param scriptChar the corresponding char in scripting.
	* @return the piece as a Byte array.
	 */
	public byte[] getPiece(char scriptChar)
	{
		try
		{
			return pieceConversionTable.get(scriptChar);
		}
		catch (NullPointerException e)
		{
			System.err.println("ERROR: piece not found! " + scriptChar + "\nQuitting...");
			System.exit(0);
			return null;
		}
	}

	/**
	* Getter for the piece movement representation for a piece.
	* @param pieceKey the key for the piece type
	* @return the movement character array.
	 */
	public char[][] getPieceMovement(byte pieceKey)
	{
		try
		{
			return pieceMovementMap.get(pieceKey);
		}
		catch (NullPointerException e) // In case key is non-existant in the map.
		{
			System.err.println("ERR: piece key inexistant in piece movement map! "
					+ pieceKey + "\nQuitting");
			System.exit(0);
			return null;
		}
	}

	/**
	* Getter for which piece the current piece promotes to, and where.
	* @param pieceKey the key of the current piece.
	* @return the promotion piece and distance to edge at which it promotes.
	 */
	public byte[] getPromotionProperties(byte pieceKey)
	{
		if (piecePromotionMap.containsKey(pieceKey))
			return piecePromotionMap.get(pieceKey);
		else return new byte[]{pieceKey, 0};
	}

	/**
	* Getter for graphic cymbol corresponding to key
	* @param pieceKey the key of the char.
	* @return the graphic/show symbol char representing the piece.
	 */
	public char getSymbol(byte pieceKey)
	{
		try
		{
			return showMap.get(pieceKey);
		}
		catch(NullPointerException e) // Catch key non-existant.
		{
			System.err.println("ERROR: Piece non-existant in show map!"
					+ pieceKey + "\nQuitting");
			System.exit(0);
			return 'n';
		}
	}

	/**
	* Getter for piece value corresponding to key.
	* @param byteKey the key of the piece.
	* @return the value of that piece.
	 */
	public int getValue (byte byteKey)
	{
		try
		{
			return pieceValueMap.get(byteKey);
		}
		catch (NullPointerException e)
		{
			System.out.println("ERROR: Piece non-existant on value map!" 
					+ byteKey + "\nQuitting...");
			System.exit(0);
			return 0;
		}
	}

	/**
	* Getter for the demote of a piece.
	* @param byteKey the key of the piece.
	* @return the value affixed to it.
	 */
	public byte getDemote (byte byteKey)
	{
		try
		{
			return demoteMap.get(byteKey);
		}
		catch (NullPointerException e)
		{
			return byteKey;
		}
	}

	@Override
	public void onObject(HashMap<String, String> currObject) 
	{
		try // To catch null pointer exceptions on maps generated by script parsing.
		{
			// Add a piece to the piece array and piece conversion table.
			if (currObject.get("name").equals("pieces"))
			{
				// Define piece key and piece characters.
				byte pieceKey = 
					LogicHandler.intToByte(Integer.valueOf(currObject.get("byteKey")));
				char[] pieceChars = new char[]
				{
					currObject.get("char_0").toCharArray()[0],
					currObject.get("char_1").toCharArray()[0]
				};
				// Add characters to the piece conversion table.
				pieceConversionTable.put(pieceChars[0], new byte[]{pieceKey, 1});
				pieceConversionTable.put(pieceChars[1], new byte[]{pieceKey, 2});

				// Add piece promotion to the promotion map.
				if (currObject.containsKey("promotion"))
				{
					byte dist = 1;

					// If promotion distance is present, add it.
					if (currObject.containsKey("promotion_dist"))
					{
						String distString = currObject.get("promotion_dist");
						dist = LogicHandler.intToByte(Integer.valueOf(distString));
					}

					// Add the piece promotion key to the promotion map.
					String promoString = currObject.get("promotion");
					byte promo = LogicHandler.intToByte(Integer.valueOf(promoString));
					piecePromotionMap.put(pieceKey, new byte[] {promo, dist});
				}

				// Add piece's value to value map.
				if (currObject.containsKey("value"))
				{
					String rewardString = currObject.get("value");
					if (rewardString.matches("[0-9]*?")) // Is a number
						pieceValueMap.put(pieceKey, Integer.valueOf(rewardString));
				}

				// Add piece to demote map.
				if (currObject.containsKey("demote"))
				{
					if (currObject.get("demote").matches("[0-9]*?"))
						demoteMap.put(pieceKey, Byte.valueOf(currObject.get("demote")));
				}

				// Add movement array to the piece on the piece movement map.
				int iters = Integer.valueOf(currObject.get("placement_iter"));
				char[][] movementArray = new char[iters][];
				for (int i = 0; i < iters; i++)
				{
					String keyString = "placement_" + i;
					movementArray[i] = currObject.get(keyString).toCharArray();
				}
				pieceMovementMap.put(pieceKey, movementArray);

				// Add show character to the show map.
				char showChar = currObject.get("show").toCharArray()[0];
				showMap.put(pieceKey, showChar);
				
			}
		}
		catch (NullPointerException e) // In case of null pointer exception caused by wrong maps.
		{
			System.out.println("ERROR : On piece registration");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/** Initializes the game rules object. */
	public void init()
	{
		pieceMovementMap = new HashMap<>(); // Reset piece movements
		pieceConversionTable = new HashMap<>(); // Reset piece conversion
		pieceConversionTable.put('0', new byte[]{0, 0}); // Add empty conversion.
		piecePromotionMap = new HashMap<>(); // Reset piece promotion.
		pieceValueMap = new HashMap<>(); // Reset piece value
		demoteMap = new HashMap<>(); // Reset demote map.
		showMap = new HashMap<>(); // Reset graphics for pieces.
		showMap.put((byte) 0, ' '); // Add graphic for empty case.
	}

	/** Constructor for the game rules class. */
	public GameRules()
	{
		init();
	}
}
