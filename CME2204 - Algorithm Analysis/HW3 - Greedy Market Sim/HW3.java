import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static final int MAX_TYPE = 100;//max type sayısı

    static class Scenario {
        int baseCost;
        int cashierCount;
        int maxTypesPerCashier;
        List<Integer> transactions;

        public Scenario(int baseCost, int cashierCount, int maxTypesPerCashier, List<Integer> transactions) {
            this.baseCost = baseCost;
            this.cashierCount = cashierCount;
            this.maxTypesPerCashier = maxTypesPerCashier;
            this.transactions = transactions;
        }
    }

    static class Cashier {
        boolean[] knownTypes = new boolean[MAX_TYPE];//bilinen typeları tutar
        int knownCount = 0;         //bilinen typleri sayar
        int last1 = -1;             //son type
        int last2 = -1;             //sondan önceki type
        int maxTypeSeen = -1;       //görülen max type
        int servedCount = 0;        //kasiyerın toplam işlem sayısı
        double totalCost = 0;       //bu işlemlerin toplam maliyeti


        //kasiyerin bu işlemi daha önce gördü veya işlem hakkı var mı
        boolean canHandle(int type, int k) {
            return knownTypes[type] || knownCount < k;
        }

        //daha büyük type görmüş mü kontrol eder
        boolean hasSeenBigger(int type) {
            return maxTypeSeen > type;
        }

        //ilk kez karşılaşan typei işaretler
        void markType(int type) {
            if (!knownTypes[type]) {
                knownTypes[type] = true;
                knownCount++;
            }
            if (type > maxTypeSeen) {
                maxTypeSeen = type;
            }
        }

        //son typeleri değiştirir
        void pushHistory(int type) {
            last2 = last1;
            last1 = type;
            servedCount++;
        }
    }

    public static void main(String[] args) throws IOException {
        List<Scenario> scenarios = readInput("src/input.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/output.txt"));

        for (int i = 0; i < scenarios.size(); i++) {
            Scenario scenario = scenarios.get(i);
            double result = solveScenario(scenario);
            writer.write(String.format("%.2f", result));
            writer.newLine();
        }

        writer.close();
    }



    static List<Scenario> readInput(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<Scenario> scenarioList = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {

            int base = Integer.parseInt(line.trim());
            int count = Integer.parseInt(br.readLine().trim());
            int limit = Integer.parseInt(br.readLine().trim());

            String[] parts = br.readLine().split(", ");
            List<Integer> list = new ArrayList<>();     //typelar için
            for (int i = 0; i < parts.length; i = i + 1) {
                String[] parts0 = parts[i].split(" ");
                list.add(Integer.parseInt(parts0[1]));
            }

            scenarioList.add(new Scenario(base, count, limit, list));
        }

        br.close();
        return scenarioList;
    }

    static double solveScenario(Scenario scenario) {

        int n = scenario.cashierCount;
        int k = scenario.maxTypesPerCashier;
        int baseCost = scenario.baseCost;
        List<Integer> transactions = scenario.transactions;

        Cashier[] cashiers = new Cashier[n];
        for (int i = 0; i < n; i++) cashiers[i] = new Cashier();

        int globalCount = 0;
        int inflationBonus = 0;

        for (int j = 0; j < transactions.size(); j++) {
            int type = transactions.get(j);

            double minCost = 100000;
            int best = -1;

            // tüm kasiyerler üzerinde dolaşır
            for (int i = 0; i < cashiers.length; i++) {
                Cashier cashier = cashiers[i];

                if (cashier.canHandle(type, k)) {
                    double cost = 0;

                    // type değişimi
                    if (cashier.last1 != -1 && cashier.last1 != type) {
                        cost =cost+ baseCost + inflationBonus;
                    }

                    // exhaustion kontrol
                    if (cashier.last1 == type && cashier.last2 == type) {
                        double normalCost = baseCost + inflationBonus;
                        if (cost == 0) {
                            cost = normalCost;
                        }
                        cost = cost * 1.5;
                    }


                    // learning curve kontrolü
                    if (cashier.hasSeenBigger(type)) {
                        cost = cost * 0.8;
                    }

                    // minx cost ve tie-breaker kontrolü
                    if (cost < minCost) {
                        minCost = cost;
                        best = i;
                    } else if (cost == minCost && i < best) {
                        best = i;
                    }


                }
            }


            // atanamayan işlem
            if (best == -1) return -1.0;

            // seçilen kasiyeri günceller
            Cashier chosen = cashiers[best];
            chosen.markType(type);
            chosen.pushHistory(type);
            chosen.totalCost = chosen.totalCost+minCost;

            //enflasyon kontrolü
            globalCount++;
            if (globalCount % 5 == 0) inflationBonus++;
        }

        double total = 0.0;
        for (int i = 0; i < cashiers.length; i++) {
            total =total+ cashiers[i].totalCost;
        }
        return total;
    }
}
