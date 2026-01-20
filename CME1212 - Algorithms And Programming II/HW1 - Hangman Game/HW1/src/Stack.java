public class Stack {
	private int top;
	private Object[] elements;

	Stack(int capacity) {
		elements = new Object[capacity];
		top = -1;
	}

	boolean isFull() {
		return (top + 1 == elements.length);
	}

	boolean isEmpty() {
		return (top == -1);
	}

	void Push(Object data) {
		if (isFull())
			System.out.println("StackOverflow");
		else {
			top++;
			elements[top] = data;
		}
	}

	Object Peek() {
		if (isEmpty()) {
			System.out.println("empty stack");
			return null;
		} else
			return elements[top];
	}

	Object Pop() {
		if (isEmpty()) {
			System.out.println("empty stackPOP");
			return null;
		} else {
			Object retData=elements[top];
			elements[top]=null;
			top--;
			return retData;
		}
		
		
	}
		
	int Size() {
		return top+1;
	}

}
