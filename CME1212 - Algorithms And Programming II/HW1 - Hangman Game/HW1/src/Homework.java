import java.util.Scanner;
import java.io.File;
import java.util.Random;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class Homework {

	public static void main(String[] args) throws IOException {
		char playAgain = 'Y';
		int newScore;
		String newPlayer;
		boolean scoreflag;

		Stack AnimalStack = new Stack(14);
		Stack LetterStack = new Stack(26);
		Stack TempLetter = new Stack(26);
		Stack TempPrinted = new Stack(12);
		Stack MissedLetters = new Stack(12);
		Stack BoardStack = new Stack(15);
		Stack BoardPrinted = new Stack(15);
		Stack TempMissed = new Stack(19);
		Stack TempBoard = new Stack(12);

		File animals = new File("animals.txt");
		if (!animals.exists()) {
			animals.createNewFile();
		}

		File dosya = new File("C:\\animals.txt");
		Scanner sc = new Scanner(dosya);

		while (sc.hasNextLine()) {
			String satir = sc.nextLine();
			satir = satir.toUpperCase();
			AnimalStack.Push(satir);
		}

		sc.close();

		while (playAgain == 'Y') {

			Scanner scanner = new Scanner(System.in);
			String guessLetter0 = "00";
			char guessLetter = '0';
			int score = 120;
			Random random = new Random();
			int numberForJoker = 0;
			boolean flagForJoker = true;
			int count = 0;

			int letterCounter = 0;

			LetterStack.Push('Z');
			LetterStack.Push('Y');
			LetterStack.Push('X');
			LetterStack.Push('W');
			LetterStack.Push('V');
			LetterStack.Push('U');
			LetterStack.Push('T');
			LetterStack.Push('S');
			LetterStack.Push('R');
			LetterStack.Push('Q');
			LetterStack.Push('P');
			LetterStack.Push('O');
			LetterStack.Push('N');
			LetterStack.Push('M');
			LetterStack.Push('L');
			LetterStack.Push('K');
			LetterStack.Push('J');
			LetterStack.Push('I');
			LetterStack.Push('H');
			LetterStack.Push('G');
			LetterStack.Push('F');
			LetterStack.Push('E');
			LetterStack.Push('D');
			LetterStack.Push('C');
			LetterStack.Push('B');
			LetterStack.Push('A');

			String word = (String) AnimalStack.Peek();
			for (int i = 0; i < word.length(); i++) {
				BoardStack.Push(word.charAt(i));
				BoardPrinted.Push('-');
			}

			while (true) {

				boolean missed = true;
				boolean board = true;

				while (!BoardPrinted.isEmpty()) {
					System.out.print(BoardPrinted.Peek() + " ");
					TempPrinted.Push(BoardPrinted.Pop());
				}

				System.out.print("       Misses: ");

				while (!TempMissed.isEmpty()) {
					System.out.print(TempMissed.Peek() + " ");
					MissedLetters.Push(TempMissed.Pop());
				}

				System.out.print("     Score: " + score + "   ");

				while (!LetterStack.isEmpty()) {
					TempLetter.Push(LetterStack.Pop());
					System.out.print(TempLetter.Peek());
				}

				System.out.println();

				if (score <= 0) {
					System.out.println();
					newScore = score;
					System.out.println("You Lost!!");
					System.out.println("Your Score is " + newScore + ".");
					scoreflag = false;
					break;
				}

				if (letterCounter == BoardStack.Size()) {
					System.out.println();
					newScore = score;
					System.out.println("You Win!!");
					System.out.println("Your Score is " + newScore + ".");
					scoreflag = true;
					break;
				}

				letterCounter = 0;

				System.out.print("Guess: ");

				guessLetter0 = scanner.next();

				if (guessLetter0.equals("Joker") && count == 0 && flagForJoker == true) {

					boolean flag = false;
					while (count < word.length()) {

						numberForJoker = random.nextInt(word.length());

						for (int i = 0; i <= numberForJoker; i++) {
							BoardPrinted.Push(TempPrinted.Pop());
							TempBoard.Push(BoardStack.Pop());
						}
						if ((Character) BoardPrinted.Peek() == '-') {
							guessLetter = (Character) TempBoard.Peek();
							flag = true;
							count = word.length();
							flagForJoker = false;
						}

						Pusher(TempBoard, BoardStack);
						Pusher(BoardPrinted, TempPrinted);
						count++;
					}
				} else if (flagForJoker == false && guessLetter0.equals("Joker")) {
					System.out.println("You can use joker for once -_-");
					guessLetter = '0';
					count = 0;
				} else
					guessLetter = (Character) guessLetter0.charAt(0);

				while (!MissedLetters.isEmpty()) {
					if ((Character) MissedLetters.Peek() == guessLetter) {
						System.out.println("You entered the same letter before.");
					}
					TempMissed.Push(MissedLetters.Pop());
				}
				Pusher(TempMissed, MissedLetters);

				while (!BoardStack.isEmpty()) {
					if ((Character) BoardStack.Peek() == guessLetter) {
						BoardPrinted.Push(BoardStack.Peek());
						TempPrinted.Pop();
						board = false;
						letterCounter++;
					} else if ((Character) TempPrinted.Peek() == '-') {
						BoardPrinted.Push(TempPrinted.Pop());
					} else {
						BoardPrinted.Push(TempPrinted.Pop());
						letterCounter++;
					}
					TempBoard.Push(BoardStack.Pop());
				}

				while (!TempMissed.isEmpty()) {
					if ((Character) TempMissed.Peek() == guessLetter) {
						missed = false;
					}
					MissedLetters.Push(TempMissed.Pop());
				}

				while (!TempLetter.isEmpty()) {
					if ((Character) TempLetter.Peek() != guessLetter) {
						LetterStack.Push(TempLetter.Pop());
					} else if (missed = true && board == true) {
						MissedLetters.Push(TempLetter.Pop());
						if (guessLetter == 'A' || guessLetter == 'E' || guessLetter == 'I' || guessLetter == 'O'
								|| guessLetter == 'U') {
							score -= 15;
						} else
							score -= 20;
					}

					else
						TempLetter.Pop();
				}

				Pusher(MissedLetters, TempMissed);
				Pusher(TempBoard, BoardStack);
				Pusher(TempPrinted, BoardPrinted);

			}

			if (scoreflag) {

				System.out.println("What is your name: ");
				newPlayer = scanner.next();

				String highScorePath = "C:\\highscoretable.txt";

				File scoreTable = new File(highScorePath);
				if (!scoreTable.exists()) {
					scoreTable.createNewFile();
				}

				FileWriter fileWriter = new FileWriter(highScorePath, true);
				fileWriter.write(newPlayer + " " + newScore + "\n");
				fileWriter.close();

				CircularQueue FirstNames = new CircularQueue(50);
				CircularQueue FirstScores = new CircularQueue(50);
				FileReader scoreReader = new FileReader(highScorePath);
				Scanner scoreScanner = new Scanner(scoreReader);

				while (scoreScanner.hasNextLine()) {
					String str = scoreScanner.nextLine();
					FirstNames.enqueue(str.split(" ")[0]);
					FirstScores.enqueue((str.split(" ")[1]));
				}
				scoreScanner.close();

				int lenght = 12;
				if (lenght > FirstNames.size())
					lenght = FirstNames.size();

				CircularQueue Scores = new CircularQueue(12);
				CircularQueue Names = new CircularQueue(12);
				int rear = 120;
				int counter = 0;
				int i = 0;
				while (counter < lenght) {

					if (Integer.valueOf(FirstScores.peek().toString()) == rear) {
						Scores.enqueue(FirstScores.dequeue());
						Names.enqueue(FirstNames.dequeue());
						counter++;
					} else {
						FirstScores.enqueue(FirstScores.dequeue());
						FirstNames.enqueue(FirstNames.dequeue());
					}
					i++;
					if (i == lenght) {
						i = 0;
						rear = rear - 5;
					}
				}

				System.out.println("\nHigh Score Table\n");

				while (!Names.isEmpty()) {
					System.out.println(Names.dequeue() + " " + Scores.dequeue());
				}

			}
			System.out.println();
			System.out.println("Play Again?");
			String playAgain0;
			playAgain0 = scanner.next();
			playAgain = (Character) playAgain0.charAt(0);

			if (playAgain == 'Y') {

				AnimalStack.Pop();
				EmptyMaker(LetterStack);
				EmptyMaker(TempLetter);
				EmptyMaker(TempPrinted);
				EmptyMaker(MissedLetters);
				EmptyMaker(BoardStack);
				EmptyMaker(BoardPrinted);
				EmptyMaker(TempMissed);
				EmptyMaker(TempBoard);

			}
		}
	}

	static void Pusher(Stack a, Stack b) {
		while (!a.isEmpty()) {
			b.Push(a.Pop());
		}
	}

	static void EmptyMaker(Stack a) {
		while (!a.isEmpty()) {
			a.Pop();
		}
	}
}
