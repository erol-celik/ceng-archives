
public class DoubleLinkedList {
	private DNode head;
	private DNode tail;

	public DoubleLinkedList() {
		head = null;
		tail = null;
	}

	public void addSorted(String data) {
		if (head == null && tail == null) {
		    DNode newDNode = new DNode(data);
		    head = newDNode;
		    tail = newDNode;
		} else if (Integer.parseInt(data.split(" ")[1]) < Integer.parseInt(head.getData().toString().split(" ")[1])) {
		    DNode newDNode = new DNode(data);
		    newDNode.setNext(head);
		    head.setPrev(newDNode);
		    head = newDNode;
		} else {
		    DNode newDNode = new DNode(data);
		    DNode temp = head;
		    while (temp.getNext() != null && Integer.parseInt(data.split(" ")[1]) > Integer.parseInt(temp.getNext().getData().toString().split(" ")[1])) {
		        temp = temp.getNext();
		    }
		    newDNode.setPrev(temp);
		    newDNode.setNext(temp.getNext());
		    if (temp.getNext() != null)
		        temp.getNext().setPrev(newDNode);
		    else
		        tail = newDNode;
		    temp.setNext(newDNode);
		}
	}
	
	public void display() {      
		if(head == null) {
			System.out.println("Empty List");
		}
		else {
			DNode temp = head;
			while(temp!= null) {
				System.out.println(temp.getData());
				temp = temp.getNext();
			}
		}
	}
}
