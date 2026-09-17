package dsa;

import model.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Hand-written searching algorithms.
 */
public final class SearchUtils {

    private SearchUtils() { }

    /** O(n) linear search: returns all expenses whose title/note/category contains keyword (case-insensitive). */
    public static List<Expense> linearSearchByKeyword(List<Expense> expenses, String keyword) {
        List<Expense> result = new ArrayList<>();
        String key = keyword.toLowerCase();
        for (Expense e : expenses) {
            if (e.getTitle().toLowerCase().contains(key)
                    || e.getCategory().toLowerCase().contains(key)
                    || (e.getNote() != null && e.getNote().toLowerCase().contains(key))) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * O(log n) binary search for an exact date.
     * PRECONDITION: the list must already be sorted by date ascending
     * (use SortUtils.mergeSort/quickSort with a date comparator first).
     * Returns the index of the FIRST expense matching that date, or -1.
     */
    public static int binarySearchByDate(List<Expense> sortedExpenses, LocalDate targetDate) {
        int low = 0, high = sortedExpenses.size() - 1;
        int foundIndex = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            LocalDate midDate = sortedExpenses.get(mid).getDate();
            int cmp = midDate.compareTo(targetDate);

            if (cmp == 0) {
                foundIndex = mid;
                high = mid - 1; // keep searching left for the first occurrence
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return foundIndex;
    }

    /** Collects every expense on targetDate using binary search to find the first match, then scanning forward. */
    public static List<Expense> allOnDate(List<Expense> sortedExpenses, LocalDate targetDate) {
        List<Expense> result = new ArrayList<>();
        int idx = binarySearchByDate(sortedExpenses, targetDate);
        if (idx == -1) return result;
        while (idx < sortedExpenses.size() && sortedExpenses.get(idx).getDate().equals(targetDate)) {
            result.add(sortedExpenses.get(idx));
            idx++;
        }
        return result;
    }

    /**
     * O(n) linear scan for every expense in a given year (and, optionally, a given month).
     * Pass month == null to match the whole year. Powers the "Monthly" / "Yearly" views.
     */
    public static List<Expense> filterByYearMonth(List<Expense> expenses, int year, Integer month) {
        List<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            boolean yearMatches = e.getDate().getYear() == year;
            boolean monthMatches = (month == null) || (e.getDate().getMonthValue() == month);
            if (yearMatches && monthMatches) {
                result.add(e);
            }
        }
        return result;
    }

    /** O(n) linear scan for every expense that falls on the given calendar date (java.time.LocalDate.now() typically). */
    public static double sumOnDate(List<Expense> expenses, LocalDate date) {
        double total = 0;
        for (Expense e : expenses) {
            if (e.getDate().equals(date)) total += e.getAmount();
        }
        return total;
    }
}
