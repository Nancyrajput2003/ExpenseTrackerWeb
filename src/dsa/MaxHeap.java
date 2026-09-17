package dsa;

import model.Expense;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written binary max-heap (array-based) keyed on Expense.amount.
 * Used to answer "what are the top-N biggest expenses" in O(log n) per insert
 * and O(k log n) to extract the top k, instead of sorting the whole list.
 */
public class MaxHeap {

    private final List<Expense> heap = new ArrayList<>();

    public void insert(Expense e) {
        heap.add(e);
        siftUp(heap.size() - 1);
    }

    public boolean isEmpty() { return heap.isEmpty(); }

    public int size() { return heap.size(); }

    /** Removes and returns the current largest expense — O(log n). */
    public Expense extractMax() {
        if (heap.isEmpty()) return null;
        Expense max = heap.get(0);
        Expense last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return max;
    }

    /** Peek at the largest without removing it. */
    public Expense peekMax() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /**
     * Returns the top N expenses by amount WITHOUT mutating this heap
     * (works on a throwaway copy so the manager can call this repeatedly).
     */
    public List<Expense> topN(int n) {
        MaxHeap copy = new MaxHeap();
        copy.heap.addAll(this.heap);
        List<Expense> result = new ArrayList<>();
        int count = Math.min(n, copy.size());
        for (int i = 0; i < count; i++) {
            result.add(copy.extractMax());
        }
        return result;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap.get(i).getAmount() > heap.get(parent).getAmount()) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    private void siftDown(int i) {
        int n = heap.size();
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int largest = i;

            if (left < n && heap.get(left).getAmount() > heap.get(largest).getAmount()) largest = left;
            if (right < n && heap.get(right).getAmount() > heap.get(largest).getAmount()) largest = right;

            if (largest == i) break;
            swap(i, largest);
            i = largest;
        }
    }

    private void swap(int i, int j) {
        Expense temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
