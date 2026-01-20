import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Main {

    public static void main(String[] args) {

        GeneralTree<Species> tree = new GeneralTree<>();
        Map<Integer, GeneralTree.TreeNode<Species>> nodeMap = new HashMap<>();

        menu(tree,nodeMap);
    }

    public static void menu(GeneralTree<Species> tree, Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        Scanner scannerMenu = new Scanner(System.in);
        boolean menuFlag = true;
        int operation;
        while (menuFlag) {
            System.out.println("\n1 > Insert Medias");
            System.out.println("2 > Search for Species ");
            System.out.println("3 > Traverse the tree int pre-order and write into file ");
            System.out.println("4 > Subtree of the Species");
            System.out.println("5 > Ancestor path of the Species");
            System.out.println("6 > Most Recent Common Ancestor of Two Species");
            System.out.println("7 > Height, Degree and Breadth of Tree");
            System.out.println("8 > Longest Evolutionary Path/Paths");
            System.out.println("\n0 > Exit");
            System.out.println("\nPlease Choose an Operation");
            operation = scannerMenu.nextInt();
            System.out.println();

            if (operation == 1) loadTreeData(tree, nodeMap);
            else if (operation == 2) searchSpecies(nodeMap);
            else if (operation == 3) writePreOrder(tree);
            else if (operation == 4) printSubtreePreOrder(tree, nodeMap);
            else if (operation == 5) ancestorPath(nodeMap);
            else if (operation == 6) commonAncestor(tree,nodeMap);
            else if (operation == 7) calculateHDB(tree);
            else if (operation == 8) printLongestPaths(tree);
            else if (operation == 0) menuFlag = false;
            else System.out.println("Enter a Valid Operaiton!");
            System.out.println("\n------------------------------------------------------------");

        }
    }

    public static void calculateHDB(GeneralTree<Species> tree) {
        if (tree == null || tree.isEmpty()) {
            System.out.println("The tree is empty!");
            return;
        }

        Queue<GeneralTree.TreeNode<Species>> nodeQueue = new LinkedList<>();
        Queue<Integer> levelQueue = new LinkedList<>();

        nodeQueue.add(tree.getRootNode());
        levelQueue.add(1);

        int maxHeight = 0;
        int maxDegree = 0;
        int breadth = 0;

        while (!nodeQueue.isEmpty()) {
            GeneralTree.TreeNode<Species> currentNode = nodeQueue.poll();
            int currentLevel = levelQueue.poll();

            maxHeight = Math.max(maxHeight, currentLevel);
            maxDegree = Math.max(maxDegree, currentNode.getChildren().size());//checks every node for num of children

            if (currentNode.getChildren().isEmpty()) {
                breadth++;
            }

            for (GeneralTree.TreeNode<Species> child : currentNode.getChildren()) {//next nodes
                nodeQueue.add(child);
                levelQueue.add(currentLevel + 1);
            }
        }

        System.out.println("Height of the tree: " + maxHeight);
        System.out.println("Degree of the tree: " + maxDegree);
        System.out.println("Breadth of the tree: " + breadth);
    }

    public static void printLongestPaths(GeneralTree<Species> tree) {
        if (tree == null || tree.isEmpty()) {
            System.out.println("The tree is empty!");
            return;
        }

        Stack<GeneralTree.TreeNode<Species>> nodeStack = new Stack<>();//tree
        Stack<List<Species>> pathStack = new Stack<>();//for travel
        List<List<Species>> longestPaths = new ArrayList<>();//for hold
        int maxLength = 0;

        nodeStack.push(tree.getRootNode());
        pathStack.push(new ArrayList<>(Collections.singletonList(tree.getRootNode().getData())));

        while (!nodeStack.isEmpty()) {//pathleri dolasiyor ve uzunlugunu kıyaslıyor ve ona gore stacke atıyor
            GeneralTree.TreeNode<Species> currentNode = nodeStack.pop();
            List<Species> currentPath = pathStack.pop();

            if (currentNode.getChildren().isEmpty()) {//cocuk yoksa ataya donuyo
                if (currentPath.size() > maxLength) {
                    maxLength = currentPath.size();
                    longestPaths.clear();
                    longestPaths.add(currentPath);
                } else if (currentPath.size() == maxLength) {
                    longestPaths.add(currentPath);
                }
            } else {//sonraki cocuga geçiyor
                int childCount = currentNode.getChildren().size();
                for (int i = childCount - 1; i >= 0; i--) {
                    GeneralTree.TreeNode<Species> child = currentNode.getChildren().get(i);
                    List<Species> newPath = new ArrayList<>(currentPath);
                    newPath.add(child.getData());
                    nodeStack.push(child);
                    pathStack.push(newPath);
                }
            }
        }

        System.out.println("Longest Evolutionary Path(s):");//print paths
        for (int i = 0; i < longestPaths.size(); i++) {
            List<Species> path = longestPaths.get(i);
            System.out.print(path.size() + " ---> ");
            for (int j = 0; j < path.size(); j++) {
                if (j > 0) System.out.print(" -> ");
                System.out.print(path.get(j).getId() + "-" + path.get(j).getName());
            }
            System.out.println();
        }
    }

    public static void commonAncestor(GeneralTree<Species> tree, Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("16421 16954 given in assignment sheet");
        System.out.print("Enter the first species ID: ");
        int species1Id = scanner.nextInt();
        System.out.println(nodeMap.get(species1Id).getData().getName());
        System.out.print("Enter the second species ID: ");
        int species2Id = scanner.nextInt();
        System.out.println(nodeMap.get(species2Id).getData().getName() + "\n");

        GeneralTree.TreeNode<Species> species1Node = nodeMap.get(species1Id);
        GeneralTree.TreeNode<Species> species2Node = nodeMap.get(species2Id);

        if (species1Node == null || species2Node == null) {
            System.out.println("One or both species not found in the tree!");
            return;
        }

        int height1 = getNodeHeight(species1Node);
        int height2 = getNodeHeight(species2Node);

        while (height1 > height2) {
            species1Node = species1Node.getParent();
            height1--;
        }
        //makes heights equal
        while (height2 > height1) {
            species2Node = species2Node.getParent();
            height2--;
        }

        while (species1Node != null && species2Node != null) {//aynı çıkana kadar ağaçta yukarı dogru ilerliyor
            if (species1Node == species2Node) {
                System.out.println("The most recent common ancestor of " +
                        species1Id + "-" + species1Node.getData().getName() + " and " +
                        species2Id + "-" + species2Node.getData().getName() +
                        " is " + species1Node.getData().getId() + "-" + species1Node.getData().getName() + ".");
                return;
            }
            species1Node = species1Node.getParent();
            species2Node = species2Node.getParent();
        }

        System.out.println("No common ancestor found!");
    }
    private static int getNodeHeight(GeneralTree.TreeNode<Species> node) {//köke kadar gider ve yuksekligi bulur
        int height = 0;
        while (node != null) {
            height++;
            node = node.getParent();
        }
        return height;
    }

    public static void ancestorPath(Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("16421 given in assignment sheet");
        System.out.print("Enter the species ID to print its ancestor path: ");
        int speciesId = scanner.nextInt();

        GeneralTree.TreeNode<Species> currentNode = nodeMap.get(speciesId);
        if (currentNode == null) {
            System.out.println("Species with ID " + speciesId + " not found!");
            return;
        }

        Stack<GeneralTree.TreeNode<Species>> ancestorStack = new Stack<>();//for hold whole ancestors
        while (currentNode != null) {
            ancestorStack.push(currentNode);
            currentNode = currentNode.getParent();
        }

        int level = 0;
        while (!ancestorStack.isEmpty()) {
            GeneralTree.TreeNode<Species> node = ancestorStack.pop();
            for (int i = 0; i < level; i++) {
                System.out.print("-");
            }
            System.out.println(" " + node.getData().getName() + " (+)");
            level++;
        }
    }

    public static void printSubtreePreOrder(GeneralTree<Species> tree, Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("16299 given in assignment sheet");
        System.out.print("Enter the species ID to print its subtree: ");
        int speciesId = scanner.nextInt();

        GeneralTree.TreeNode<Species> startNode = nodeMap.get(speciesId);
        if (startNode == null) {
            System.out.println("Species with ID " + speciesId + " not found!");
            return;
        }

        int currentLevel = 0;
        Stack<GeneralTree.TreeNode<Species>> nodeStack = new Stack<>();
        Stack<Integer> levelStack = new Stack<>();
        nodeStack.push(startNode);
        levelStack.push(currentLevel);

        while (!nodeStack.isEmpty()) {
            GeneralTree.TreeNode<Species> currentNode = nodeStack.pop();
            currentLevel = levelStack.pop();

            for (int i = 0; i < currentLevel; i++) {
                System.out.print("-");
            }
            if (currentNode.getChildren().isEmpty()) {
                System.out.println(currentNode.getData().getId() + "-" + currentNode.getData().getName() + " (-)");
            } else {
                System.out.println(currentNode.getData().getId() + "-" + currentNode.getData().getName() + " (+)");
            }

            List<GeneralTree.TreeNode<Species>> children = currentNode.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                nodeStack.push(children.get(i));
                levelStack.push(currentLevel + 1);
            }
        }
    }

    public static void writePreOrder(GeneralTree<Species> tree) {//iterator ile soldan saga dogru geziyor
        if (tree == null || tree.isEmpty()) {
            System.out.println("The tree is empty!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pre_order.txt"))) {
            Iterator<Species> iterator = (Iterator<Species>) tree.getPreorderIterator();
            while (iterator.hasNext()) {
                Species species = iterator.next();
                writer.write(species.getName());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
        System.out.println("Writing Completed Successfully");

    }

    public static void loadTreeData(GeneralTree<Species> tree, Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        String nodesFilePath = "C:/Users/Erol/OneDrive/Masaüstü//treeoflife_nodes.csv/";
        String linksFilePath = "C:/Users/Erol/OneDrive/Masaüstü/treeoflife_links.csv/";

        try (BufferedReader nodesReader = new BufferedReader(new FileReader(nodesFilePath))) {
            String line = nodesReader.readLine(); //skip first line
            while ((line = nodesReader.readLine()) != null) {
                String[] parts = parseCSVLine(line);

                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }

                //node prop
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int childCount = Integer.parseInt(parts[2]);
                boolean isLeaf = Integer.parseInt(parts[3]) == 1;
                String link;
                if (Integer.parseInt(parts[4]) == 1)
                    link = "http://tolweb.org/" + name.replace(" ", "") + "/" + id;
                else link = "No Available Link";
                boolean isExtinct = Integer.parseInt(parts[5]) == 1;

                String confidence;
                if (Integer.parseInt(parts[6]) == 0)
                    confidence = "Confident Position";
                else if (Integer.parseInt(parts[6]) == 1)
                    confidence = "Problematic Position";
                else confidence = "Unspecified Position";

                String phylesis;
                if (Integer.parseInt(parts[7]) == 0)
                    phylesis = "Monophyletic";
                else if (Integer.parseInt(parts[7]) == 1)
                    phylesis = "Uncertain Monophyly";
                else phylesis = "Not Monophyletic";


                //create a new specie
                Species species = new Species(id, name, childCount,
                        isLeaf, link, isExtinct, confidence, phylesis);
                GeneralTree.TreeNode<Species> node = new GeneralTree.TreeNode<>(species);
                nodeMap.put(id, node);

                if (id == 1) {//sets first line as root
                    tree.setRoot(species);
                    nodeMap.put(1, tree.getRootNode());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading nodes file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing integer: " + e.getMessage());
        }

        try (BufferedReader linksReader = new BufferedReader(new FileReader(linksFilePath))) {
            String line = linksReader.readLine(); //skip first line
            while ((line = linksReader.readLine()) != null) {
                String[] parts = line.split(",");

                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }


                //sets parents and children
                int parentId = Integer.parseInt(parts[0]);
                int childId = Integer.parseInt(parts[1]);

                GeneralTree.TreeNode<Species> parentNode = nodeMap.get(parentId);
                GeneralTree.TreeNode<Species> childNode = nodeMap.get(childId);

                if (parentNode != null && childNode != null) {
                    parentNode.getChildren().add(childNode);
                    childNode.setParent(parentNode);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading links file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing integer: " + e.getMessage());
        }
        System.out.println("Data Loaded Successfully");
    }
    public static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        char[] chars = line.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }//helps to add lines with double quote.

    public static void searchSpecies(Map<Integer, GeneralTree.TreeNode<Species>> nodeMap) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter species ID: ");
        int id = scanner.nextInt();
        GeneralTree.TreeNode<Species> node = nodeMap.get(id);
        if (node != null) {
            node.getData().printSpeice();
        } else {
            System.out.println("Species not found!");
        }
    }


}
