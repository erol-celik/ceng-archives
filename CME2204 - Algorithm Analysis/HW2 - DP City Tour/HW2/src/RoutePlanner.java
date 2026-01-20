import java.util.*;

public class RoutePlanner {

    private CityMap map;
    private double[][] dp;
    private int[][] trace;
    private int size;
    private int hotel;
    private int full;

    public RoutePlanner(CityMap map) {
        this.map = map;
        this.size = map.size;
        this.hotel = map.hotelIdx;
        this.full = (1 << size) - 1;

        dp = new double[size][1 << size];
        trace = new int[size][1 << size];

        for (int i = 0; i < size; i++) {
            Arrays.fill(dp[i], -1);
        }
    }

    public double start() {
        return dfs(hotel, 1 << hotel);
    }

    private double dfs(int current, int visited) {
        if (visited == full) {
            if (map.travelTime[current][hotel] > 0) {
                return calculateScore(current, hotel, map);
            }
            return -1000000000; //
        }

        if (dp[current][visited] != -1) {
            return dp[current][visited];
        }

        double best = -1000000000;
        for (int i = 0; i < size; i++) {
            if ((visited & (1 << i)) == 0 && map.travelTime[current][i] > 0) {
                double currScore = calculateScore(current, i, map);
                double nextScore = dfs(i, visited | (1 << i));
                double total = currScore + nextScore;

                if (total > best) {
                    best = total;
                    trace[current][visited] = i;
                }
            }
        }

        dp[current][visited] = best;
        return best;
    }


    public List<Integer> getPath() {
        List<Integer> path = new ArrayList<>();
        int curr = hotel;
        int mask = 1 << hotel;
        path.add(curr);
        while (mask != full) {
            int next = trace[curr][mask];
            path.add(next);
            mask |= (1 << next);
            curr = next;
        }
        path.add(hotel);
        return path;
    }

    public double calculateScore(int from, int to, CityMap map) {
        double base = map.baseScore[from][to];
        double time = map.travelTime[from][to];
        double inter = map.interest[to];
        double vis = map.load[to];

        double rate = 1 - vis * 0.03 * time;
        if (rate < 0.1) rate = 0.1;

        return base * inter * rate;
    }
}
