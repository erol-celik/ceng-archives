import java.io.*;
import java.util.*;

public class CityMap {

    public int size;
    public String[] names;
    public Map<String, Integer> indexMap;

    public double[][] travelTime;
    public double[][] baseScore;

    public double[] interest;
    public double[] load;

    public int hotelIdx = -1;

    public CityMap(int n) {
        this.size = n;
        names = new String[n];
        indexMap = new HashMap<>();
        travelTime = new double[n][n];
        baseScore = new double[n][n];
        interest = new double[n];
        load = new double[n];
    }

    public void readMap(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        br.readLine(); // skip fisrt line
        String line;
        int index = 0;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            String from = parts[0];
            String to = parts[1];
            double score = Double.parseDouble(parts[2]);
            double time = Double.parseDouble(parts[3]);

            if (!indexMap.containsKey(from)) {
                indexMap.put(from, index);
                names[index] = from;
                if (from.equalsIgnoreCase("Hotel")) hotelIdx = index;
                index++;
            }
            if (!indexMap.containsKey(to)) {
                indexMap.put(to, index);
                names[index] = to;
                if (to.equalsIgnoreCase("Hotel")) hotelIdx = index;
                index++;
            }

            int i = indexMap.get(from);
            int j = indexMap.get(to);
            travelTime[i][j] = time;
            baseScore[i][j] = score;
        }
        br.close();
    }

    public void readLoad(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            String name = parts[0];
            double val = Double.parseDouble(parts[1]);
            if (indexMap.containsKey(name)) {
                int i = indexMap.get(name);
                load[i] = val;
            }
        }
        br.close();
    }

    public void readInterest(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            String name = parts[0];
            double val = Double.parseDouble(parts[1]);
            if (indexMap.containsKey(name)) {
                int i = indexMap.get(name);
                interest[i] = val;
            }
        }
        br.close();
    }

}
