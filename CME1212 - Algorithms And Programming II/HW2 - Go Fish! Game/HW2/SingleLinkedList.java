
public class SingleLinkedList {
	Node head;

	// sorted
	public void sortedAdd(Object dataToAdd) {

		if (head == null) { // list is empty
			Node newNode = new Node(dataToAdd);
			head = newNode;
		} else if ((Integer) dataToAdd <= (Integer) head.getData()) { // insert at front
			Node newnode = new Node(dataToAdd);
			newnode.setLink(head);
			head = newnode;
		}

		else { // insert in between or insert at last
			Node temp = head;
			Node previous = null;

			while (temp != null && (Integer) dataToAdd >= (Integer) temp.getData()) {
				previous = temp;
				temp = temp.getLink();
			}
			if (temp == null) // insert at last
			{
				Node newnode = new Node(dataToAdd);
				previous.setLink(newnode);
			} else { // insert in between
				Node newnode = new Node(dataToAdd);
				newnode.setLink(previous.getLink());
				previous.setLink(newnode);
			}
		}
	}

	public void unsortedAdd(Object dataToAdd) {
		if (head == null) {
			Node newnode = new Node(dataToAdd);
			head = newnode;
		} else {
			Node temp = head;
			while (temp.getLink() != null) {
				temp = temp.getLink();
			}
			Node newnode = new Node(dataToAdd);
			temp.setLink(newnode);
		}
	}

	public int size() {
		if (head == null)
			return 0;
		else {
			int counter = 0;
			Node temp = head;
			while (temp != null) {
				temp = temp.getLink();
				counter++;
			}
			return counter;
		}
	}

	public void display() {
		if (head == null)
			return;
		else {
			Node temp = head;
			while (temp != null) {
				System.out.print(temp.getData() + " ");
				temp = temp.getLink();
			}
		}
	}

	public void delete(Object dataToDelete) {
		if (head == null) {

		} else {

			if (head.getData().equals(dataToDelete)) {
				head = head.getLink();
			} else {
				Node temp = head;
				Node previous = null;
				while (temp != null) {

					if (temp.getData().equals(dataToDelete)) {

						previous.setLink(temp.getLink());
						break;
					}
					previous = temp;
					temp = temp.getLink();
				}
			}
		}
	}

	public boolean search(Object item) {
		boolean flag = false;
		if (head != null)
		{
			Node temp = head;
			while (temp != null) {
				if (item == temp.getData())
					flag = true;
				temp = temp.getLink();
			}
		}
		return flag;
	}

}
