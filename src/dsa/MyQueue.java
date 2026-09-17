package dsa;

import java.util.NoSuchElementException;

/**
 * Hand-written circular array queue (FIFO) used for the
 * recurring-expense reminder pipeline. Auto-resizes when full.
 */
public class MyQueue<T> {

    private Object[] data;
    private int front;
    private int rear;
    private int size;

    public MyQueue() {
        this(8);
    }

    public MyQueue(int capacity) {
        data = new Object[capacity];
        front = 0;
        rear = -1;
        size = 0;
    }

    public void enqueue(T value) {
        if (size == data.length) resize();
        rear = (rear + 1) % data.length;
        data[rear] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Reminder queue is empty");
        T val = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        size--;
        return val;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Reminder queue is empty");
        return (T) data[front];
    }

    public boolean isEmpty() { return size == 0; }

    public int size() { return size; }

    @SuppressWarnings("unchecked")
    private void resize() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[(front + i) % data.length];
        }
        data = newData;
        front = 0;
        rear = size - 1;
    }
}
