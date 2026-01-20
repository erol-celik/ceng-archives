import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Assignment 2 for 2022510164,Erol Çelik\n");
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter the total number of landmarks (including Hotel): ");
        int inputLandmark = sc.nextInt();

        long start = System.currentTimeMillis();

        CityMap city = new CityMap(inputLandmark);
        city.readMap("datasets/landmark_map_data.txt");
        city.readLoad("datasets/visitor_load.txt");
        city.readInterest("datasets/personal_interest.txt");

        System.out.println("\nThree input files are read.\n");
        System.out.println("The tour planning is now processing…\n");

        RoutePlanner planner = new RoutePlanner(city);
        double totalScore = planner.start();
        List<Integer> path = planner.getPath();

        double totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            totalTime += city.travelTime[from][to];
        }

        System.out.println("The visited landmarks:");
        for (int i = 0; i < path.size(); i++) {
            int index = path.get(i);
            System.out.println((i + 1) + " - " + city.names[index]);
        }

        System.out.print("\nTotal attractiveness score: "+ totalScore);
        System.out.printf("\nTotal travel time: "+ totalTime+" min.\n");

        long end = System.currentTimeMillis();
        long runtime = end - start;
        System.out.println("\nProgram took "+runtime/60000+" minutes and "+(runtime/60000)/1000+" seconds to run.\n" );
    }

}
