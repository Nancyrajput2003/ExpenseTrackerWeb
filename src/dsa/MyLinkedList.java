package dsa;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Hand-written singly linked list used as an activity/undo log.
 * New entries are pushed to the front (O(1)) so the most recent
 * action is always head — classic linked-list use case.
 */
public class MyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private int size;

    /** Insert at the front — O(1). Used for "most recent action first". */
    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        node.next = head;
        head = node;
        size++;
    }

    /** Insert at the end — O(n). */
    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = node;
        } else {
            Node<T> curr = head;
            while (curr.next != null) curr = curr.next;
            curr.next = node;
        }
        size++;
    }

    /** Remove and return the first element — O(1). Throws if empty. */
    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("Activity log is empty");
        T val = head.data;
        head = head.next;
        size--;
        return val;
    }

    public boolean isEmpty() { return head == null; }

    public int size() { return size; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> curr = head;
            @Override public boolean hasNext() { return curr != null; }
            @Override public T next() {
                if (curr == null) throw new NoSuchElementException();
                T val = curr.data;
                curr = curr.next;
                return val;
            }
        };
    }
}
