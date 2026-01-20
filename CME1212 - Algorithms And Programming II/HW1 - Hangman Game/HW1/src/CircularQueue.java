public class CircularQueue {

	private int rear;
	private int front;
	Object[] elements;
	
	public CircularQueue(int capacity) 
	{
		front=0;
		rear=-1;
		elements = new Object[capacity];
	}
	
	void enqueue(Object data) 
	{
		if(!isFull()) 
		{
			rear++;
			rear=(rear)%elements.length;
			elements[rear]=data;
		}
		else 
		{
			System.out.println("Queue is full");
		}
	}
	Object dequeue() 
	{
		if(!isEmpty()) 
		{
			Object temp = elements[front];
			elements[front]=null;
			front=(front+1)%elements.length;
			return temp;
		}
		else 
		{
			System.out.println("Queue is empty");
			return null;
		}
	}
	Object peek() 
	{
		if(!isEmpty()) 
		{
			return elements[front];
		}
		else 
		{
			System.out.println("Queue is empty");
			return null;
		}
	}
	boolean isEmpty() 
	{
		return elements[front]==null;
	}
	boolean isFull() 
	{
		if (front == ( rear + 1) % elements.length && elements[front] != null && elements[rear] != null) 
			return true;
		else return false;

	}
	int size() 
	{
			if (rear >= front)
				return rear - front + 1;
			else
				return elements.length - (front - rear) + 1;
	}


}
