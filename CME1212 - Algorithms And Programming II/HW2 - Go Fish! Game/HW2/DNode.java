public class DNode {
	private Object data;
	private DNode prev;
	private DNode next;
	public DNode(Object dataToAdd) {
		data = dataToAdd;
		prev = null;
		next = null;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public DNode getPrev() {
		return prev;
	}
	public void setPrev(DNode prev) {
		this.prev = prev;
	}
	public DNode getNext() {
		return next;
	}
	public void setNext(DNode next) {
		this.next = next;
	}
}