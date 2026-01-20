using System;
using System.IO;

namespace ödev2
{
    internal class Program
    {
        public struct Decks
        {
            public char letter;
            public int color;
            public string deck;
        }
        static void Main(string[] args)
        {
            StreamReader odev = File.OpenText("C:\\gamers.txt");  // OPENING FILE FOR GET NUMBER OF PLAYERS
            Random random = new Random();
            int number_of_players = 0;
            while (odev.ReadLine() != null)
            {
                number_of_players++;
            }

            StreamReader odevv = File.OpenText("C:\\gamers.txt");  // OPENING FILE FOR CALCULATING

            Decks[] decks = new Decks[number_of_players];
            int[] score = new int[number_of_players];
            int[] scores = new int[8];
            int high_score = 0;
            string high_scorer = "";
            string SLOT;
            string[] SLOTS = new string[number_of_players];
            string[] scores_string = new string[10];
            if (number_of_players <= 10&&number_of_players>=3)
            {
                for (int i = 0; i < number_of_players; i++)
                {
                    decks[i].deck = odevv.ReadLine();
                    SLOTS[i] = (decks[i].deck);
                    SLOT = SLOTS[i];
                    for (int c = 0; c <= 21; c += 3)
                    {
                        //////-------CHECKING CONSECETUIVE CARDS---------------

                        int letter_counter = 0;
                        int color_counter = 0;
                        if (SLOT[c] == SLOT[c + 3]) letter_counter++;  //AAX
                        if (SLOT[c] == SLOT[c + 6]) letter_counter++;  //AXA
                        if (SLOT[c + 3] == SLOT[c + 6]) letter_counter++;  //XAA
                        if (Convert.ToChar(SLOT[c]) == Convert.ToChar(SLOT[c + 3]) + 1 && Convert.ToChar(SLOT[c]) == Convert.ToChar(SLOT[c + 6]) + 2) letter_counter = 4;  //CBA
                        if (Convert.ToChar(SLOT[c + 6]) == Convert.ToChar(SLOT[c]) + 2 && Convert.ToChar(SLOT[c + 6]) == Convert.ToChar(SLOT[c + 3]) + 1) letter_counter = 4;  //ABC

                        if (SLOT[c + 1] == SLOT[c + 4]) color_counter++; //11X
                        if (SLOT[c + 1] == SLOT[c + 7]) color_counter++; //1X1
                        if (SLOT[c + 4] == SLOT[c + 7]) color_counter++; //X11

                        //////-----------------SCORING-----------------------------

                        if (letter_counter == 3) // ALL LETTERS ARE SAME
                        {
                            if (color_counter == 3) scores[c / 3] = 33; // ALL COLORS ARE SAME (A1 A1 A1)
                            else if (color_counter == 0) scores[c / 3] = 28; // ALL COLORS ARE DIFFERENT (A1 A2 A3)
                            else if (color_counter == 1) scores[c / 3] = 22; // ONLY 2 COLORS ARE SAME (A1 A1 A2)
                        }
                        else if (letter_counter == 4) // LETTERS ARE CONSECUTIVE
                        {
                            if (color_counter == 3) scores[c / 3] = 18; // ALL COLORS ARE SAME (A1 B1 C1)
                            else if (color_counter == 0) scores[c / 3] = 16; // ALL COLORS ARE DIFFERENT (A1 B2 C3)
                            else if (color_counter == 1) scores[c / 3] = 14; // ONLY 2 COLORS ARE SAME (A1 B2 C1)
                        }
                        else if (letter_counter == 0 && color_counter == 3) scores[c / 3] = 12; // DIFFERENT LETTERS WITH SAME COLOR (A1 E1 D1)
                        else if (letter_counter <= 1 && color_counter == 0) scores[c / 3] = 10; // DIFFERENT LETTERS AND DIFFERENT COLORS  (A1 A2 C4)
                        else { scores[c / 3] = 0; } // NO SCORE
                        score[i] = score[i] + scores[c / 3];
                        scores_string[i] = scores_string[i] + " /" + Convert.ToString(scores[c / 3]);
                    }

                    ////////---------------DETERMINING HIGHSCORE---------

                    if (score[i] > high_score)
                    {
                        high_score = score[i];
                        high_scorer = "PLAYER " + Convert.ToString(i + 1);
                    }
                    else if (score[i] == high_score && score[i] != 0)
                    {
                        high_score = score[i];
                        high_scorer = Convert.ToString(high_scorer) + " & PLAYER " + Convert.ToString(i + 1);
                    }
                }

                /////------PRINTING PLAYERS AND WINNER---------

                for (int i = 0; i < number_of_players; i++)
                {
                    if (score[i] == high_score && high_score > 0) Console.ForegroundColor = ConsoleColor.Cyan;
                    Console.WriteLine("PLAYER " + (1 + i) + " : " + SLOTS[i] + " ----->  " + score[i] + " points");
                    if ((score[i] == high_score && high_score > 0)) Console.ForegroundColor = ConsoleColor.DarkYellow;
                    Console.WriteLine("           " + scores_string[i]);
                    if ((score[i] == high_score && high_score > 0)) Console.ForegroundColor = ConsoleColor.White;
                    Console.WriteLine();
                }
                Console.WriteLine();

                if (high_score > 0)
                {
                    Console.Write("WINNER --> "); Console.ForegroundColor = ConsoleColor.Cyan;
                    Console.Write(high_scorer); Console.ForegroundColor = ConsoleColor.White;
                    Console.Write(" --> "); Console.ForegroundColor = ConsoleColor.DarkYellow;
                    Console.Write(high_score); Console.ForegroundColor = ConsoleColor.White;
                    Console.Write(" POINTS ");
                }
                else { Console.ForegroundColor = ConsoleColor.DarkYellow; Console.Write("NO WINNER :/"); }
                Console.ReadLine();
            }
            else { Console.WriteLine("OUT OF PLAYER LIMIT - IT SHOULD BE BETWEEN 3 AND 10 "); Console.ReadLine(); }

            odevv.Close();
        }
    }
}
