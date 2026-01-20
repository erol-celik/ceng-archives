import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Homework2 {

	static int playerBook = 0;
	static int computerBook = 0;
	static int turn = 1;
	static boolean ask = true;// true for playerturn false for computerturn

	public static void main(String[] args) throws IOException {

		SingleLinkedList table = new SingleLinkedList();
		SingleLinkedList player = new SingleLinkedList();
		SingleLinkedList computer = new SingleLinkedList();
		Scanner scanner = new Scanner(System.in);

		insertCards(table);
		first_7cards(player, table);
		first_7cards(computer, table);

		while (true) {//main game loop
			
			boolean playerNext = true;
			while (playerNext && player.size() > 0) {//player turn
				display(player, computer, table, playerBook, computerBook, turn, ask);
				int numForSearch = Integer.parseInt(scanner.next());
				boolean alreadyHave = player.search(numForSearch);
				if (alreadyHave) {
					if (goFishChecker(askToComputer(numForSearch, computer, player), ask)) {
						fishOperation(table, player);
						bookCheck(player, ask);
						playerNext = false;
					}
					turn++;
				} else
					System.out.println("Enter a card that you already have!");
			}

			if (player.size() == 0 | computer.size() == 0)//checks decks for the end
				break;
			ask = false;
			boolean computerNext = true;
			while (computerNext && computer.size() > 0) {//computer turn
				display(player, computer, table, playerBook, computerBook, turn, ask);
				if (goFishChecker(askToPlayer(randomComputerCard(computer), computer, player), ask)) {
					fishOperation(table, computer);
					bookCheck(computer, ask);
					computerNext = false;
				}
				turn++;
			}
			ask = true;

			if (player.size() == 0 | computer.size() == 0)
				break;
		}
		System.out.println("\nGame Over");

		if (winCheck()) {//get name if player wins
			Scanner scannerName = new Scanner(System.in);
			String name = scannerName.nextLine();
			Scanner score_file = null;
			try {
				score_file = new Scanner(new File("highscore.txt"));
			} catch (FileNotFoundException e) {
				FileWriter create = new FileWriter("highscore.txt");
				create.close();
			}
			FileWriter fileWriter = new FileWriter("highscore.txt", true);
			fileWriter.write(name + " " + turn + "\n");
			fileWriter.close();
		}
		
		System.out.println("High Scores~\n");// genereting high score table and displaying
		DoubleLinkedList dll= new DoubleLinkedList();
		File file = new File("highscore.txt");
		Scanner scannerScores = new Scanner(file);
		while (scannerScores.hasNextLine()) {
			String str = scannerScores.nextLine();
			dll.addSorted(str);
		}
		scannerScores.close();
		dll.display();
	}

	static void display(SingleLinkedList player, SingleLinkedList computer, SingleLinkedList table, int pb, int cb,
			int turn, boolean ask) {//displays main game screen
		System.out.println();
		System.out.print("Turn: " + turn + "                     			            Table\nYou:      ");
		player.display();
		System.out.print("        book: " + pb + "  	     ");
		table.display();
		System.out.print("\nComputer: ");
		computer.display();
		System.out.println("        book: " + cb + "         ");
		if (ask && computer.size() > 0 && player.size() > 0)
			System.out.print("You ask: ");
	}

	static void first_7cards(SingleLinkedList sll, SingleLinkedList table) {//First 7 cards from table to computer or player
		for (int j = 0; j < 7; j++) {
			int number = (int) (Math.random() * table.size());
			Node temp = table.head;
			for (int i = 1; i < number; i++) {
				temp = temp.getLink();
			}
			Object value = temp.getData();
			table.delete(value);
			sll.sortedAdd(value);
		}
	}

	static void insertCards(SingleLinkedList sll) {//First 24 cards to the table
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j < 7; j++) {
				sll.unsortedAdd(j);
			}
		}
	}

	static boolean askToComputer(int item, SingleLinkedList comp, SingleLinkedList player) {//asks a card to computer
		if (comp.search(item)) {
			comp.delete(item);
			player.sortedAdd(item);
			askToComputer(item, comp, player);
			bookCheck(player, ask);
			return true;
		} else {
			return false;
		}
	}

	static int randomComputerCard(SingleLinkedList comp) {//selects a card from computer deck
		int number = (int) (Math.random() * comp.size());
		Node temp = comp.head;
		for (int i = 1; i < number; i++) {
			temp = temp.getLink();
		}
		System.out.println("Computer asks: " + temp.getData());
		return (int) temp.getData();
	}

	static boolean askToPlayer(int item, SingleLinkedList comp, SingleLinkedList player) {//asks a card to player
		if (player.search(item)) {
			player.delete(item);
			comp.sortedAdd(item);
			askToPlayer(item, comp, player);
			bookCheck(comp, ask);
			return true;
		} else {
			return false;
		}
	}

	static boolean goFishChecker(boolean askFor, boolean ask) {//checks gofish 
		if (!askFor && ask) {
			System.out.println("Computer says 'Go Fish'");
			return true;
		} else if (!askFor && !ask) {
			System.out.println("You say 'Go Fish'");
			return true;
		} else
			return false;
	}

	static void fishOperation(SingleLinkedList table, SingleLinkedList listToAdd) {//go fish operation. selects a card randomly and add to decks
		int number = (int) (Math.random() * table.size());
		Node temp = table.head;
		for (int i = 1; i < number; i++) {
			temp = temp.getLink();
		}
		if (table.size() != 0) {
			listToAdd.sortedAdd(temp.getData());
			table.delete(temp.getData());
		}
	}

	static void bookCheck(SingleLinkedList sll, boolean ask) {//checks if there any book
		Node temp = sll.head;
		int num = (int) temp.getData();
		int count = 0;
		int t = sll.size();
		for (int i = 0; i < t; i++) {
			if ((int) temp.getData() == num) {
				count++;
				if (count == 4) {
					if (ask)
						playerBook++;
					else
						computerBook++;
					while (sll.search(num)) {
						sll.delete(num);
					}
					System.out.println(num + " " + num + " " + num + " " + num);
				}
			} else {
				num = (int) temp.getData();
				count = 1;
			}
			temp = temp.getLink();
		}
	}

	static boolean winCheck() {//end of the game checks for player win 
		if (playerBook > computerBook) {
			System.out.println("You Win!\nWhat is your name?: ");
			return true;
		} else if (playerBook == computerBook) {
			System.out.println("Tie!");
			return false;
		} else {
			System.out.println("Computer Wins!");
			return false;
		}
	}
}
