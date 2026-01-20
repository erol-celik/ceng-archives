import java.rmi.dgc.Lease;

public class Species {
    private int id;
    private String name;
    private int childCount;
    private boolean isLeaf;
    private String link;
    private boolean isExtinct;
    private String  confidence;
    private String phylesis;

    public Species(int id, String name, int childCount,
                   boolean isLeaf, String link, boolean isExtinct,
                   String confidence, String phylesis) {
        this.id = id;
        this.name = name;
        this.childCount = childCount;
        this.isLeaf = isLeaf;
        this.link = link;
        this.isExtinct = isExtinct;
        this.confidence = confidence;
        this.phylesis = phylesis;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getChildCount() {
        return childCount;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public String getLink() {
        return link;
    }

    public boolean isExtinct() {
        return isExtinct;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getPhylesis() {
        return phylesis;
    }

    public void printSpeice(){
        System.out.println("Name: "+name);
        System.out.println("Id: "+id);
        System.out.println("Child Count : "+childCount);
        System.out.print("Leaf Node: ");
        if (isLeaf) System.out.println("Yes");
        else System.out.println("No");
        System.out.println("Link: "+link);
        System.out.print("Extinct: ");
        if (isExtinct) System.out.println("Yes");
        else System.out.println("No");
        System.out.println("Confidence: "+confidence);
        System.out.println("Phylesis: "+ phylesis);
    }

    @Override
    public String toString() {
        return "Species{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", childCount=" + childCount +
                ", isLeaf=" + isLeaf +
                ", link='" + link + '\'' +
                ", isExtinct=" + isExtinct +
                ", confidence=" + confidence +
                ", phylesis=" + phylesis +
                '}';
    }
}
