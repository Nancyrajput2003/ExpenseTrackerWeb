package dsa;

import java.util.List;
import java.util.Comparator;

/**
 * Hand-written sorting algorithms (no Collections.sort / Arrays.sort shortcuts)
 * operating on a List via an index-based approach so they work for any field
 * (date, amount, category) by passing a different Comparator.
 */
public final class SortUtils {

    private SortUtils() { }

    // ---------------------- QUICK SORT ----------------------
    public static <T> void quickSort(List<T> list, Comparator<T> cmp) {
        quickSort(list, 0, list.size() - 1, cmp);
    }

    private static <T> void quickSort(List<T> list, int low, int high, Comparator<T> cmp) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, cmp);
            quickSort(list, low, pivotIndex - 1, cmp);
            quickSort(list, pivotIndex + 1, high, cmp);
        }
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<T> cmp) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // ---------------------- MERGE SORT ----------------------
    public static <T> void mergeSort(List<T> list, Comparator<T> cmp) {
        if (list.size() <= 1) return;
        mergeSort(list, 0, list.size() - 1, cmp);
    }

    @SuppressWarnings("unchecked")
    private static <T> void mergeSort(List<T> list, int left, int right, Comparator<T> cmp) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(list, left, mid, cmp);
        mergeSort(list, mid + 1, right, cmp);
        merge(list, left, mid, right, cmp);
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(List<T> list, int left, int mid, int right, Comparator<T> cmp) {
        Object[] leftArr = list.subList(left, mid + 1).toArray();
        Object[] rightArr = list.subList(mid + 1, right + 1).toArray();

        int i = 0, j = 0, k = left;
        while (i < leftArr.length && j < rightArr.length) {
            T l = (T) leftArr[i];
            T r = (T) rightArr[j];
            if (cmp.compare(l, r) <= 0) {
                list.set(k++, l);
                i++;
            } else {
                list.set(k++, r);
                j++;
            }
        }
        while (i < leftArr.length) list.set(k++, (T) leftArr[i++]);
        while (j < rightArr.length) list.set(k++, (T) rightArr[j++]);
    }
}
