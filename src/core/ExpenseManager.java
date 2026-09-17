package core;

import dsa.MaxHeap;
import dsa.MyLinkedList;
import dsa.MyQueue;
import dsa.SearchUtils;
import dsa.SortUtils;
import model.Expense;
import util.FileStorage;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The brain of the app. Deliberately keeps several data structures
 * in sync so each one can be demoed independently:
 *
 *  - ArrayList<Expense>        : primary storage (array / dynamic array)
 *  - HashMap<Integer,Expense>  : O(1) lookup of an expense by id
 *  - HashMap<String,Double>    : O(1) running total per category
 *  - MyLinkedList<String>      : most-recent-first activity log (custom linked list)
 *  - MyQueue<String>           : FIFO queue of recurring-expense reminders (custom queue)
 *  - MaxHeap                   : top-N biggest expenses (custom binary heap)
 */
public class ExpenseManager {

    private final File dataFile;

    private final List<Expense> expenses;                 // array-based storage
    private final Map<Integer, Expense> idIndex;           // hashmap: id -> expense
    private final Map<String, Double> categoryTotals;      // hashmap: category -> total
    private final MyLinkedList<String> activityLog;         // custom linked list
    private final MyQueue<String> reminderQueue;             // custom queue
    private final MaxHeap topExpensesHeap;                  // custom max-heap

    private int nextId;
    private boolean sortedByDate = false; // tracks whether `expenses` is currently date-sorted (for binary search)

    /** @param dataFile where CSV data is persisted, e.g. new File(context.getFilesDir(), "expenses.csv") */
    public ExpenseManager(File dataFile) {
        this.dataFile = dataFile;
        this.expenses = new ArrayList<>();
        this.idIndex = new HashMap<>();
        this.categoryTotals = new HashMap<>();
        this.activityLog = new MyLinkedList<>();
        this.reminderQueue = new MyQueue<>();
        this.topExpensesHeap = new MaxHeap();
        this.nextId = 1;
        loadFromDisk();
    }

    // ---------------------- CRUD ----------------------

    public Expense addExpense(String title, double amount, String category, LocalDate date, String note) {
        Expense e = new Expense(nextId++, title, amount, category, date, note);
        expenses.add(e);
        idIndex.put(e.getId(), e);
        categoryTotals.merge(category, amount, Double::sum);
        topExpensesHeap.insert(e);
        activityLog.addFirst("ADDED #" + e.getId() + " '" + title + "' Rs. " + amount + " on " + date);
        sortedByDate = false;
        persist();
        return e;
    }

    public boolean deleteExpense(int id) {
        Expense e = idIndex.remove(id);
        if (e == null) return false;
        expenses.removeIf(x -> x.getId() == id);
        categoryTotals.merge(e.getCategory(), -e.getAmount(), Double::sum);
        activityLog.addFirst("DELETED #" + id + " '" + e.getTitle() + "' Rs. " + e.getAmount());
        // A binary heap doesn't support O(log n) arbitrary-key deletion cleanly,
        // so we rebuild it from the ArrayList (source of truth) after a delete.
        rebuildHeap();
        persist();
        return true;
    }

    /** O(1) lookup by id thanks to the HashMap index. */
    public Expense getById(int id) {
        return idIndex.get(id);
    }

    public List<Expense> getAll() {
        return new ArrayList<>(expenses);
    }

    // ---------------------- SORTING ----------------------

    public void sortByDate(boolean useQuickSort) {
        Comparator<Expense> byDate = Comparator.comparing(Expense::getDate);
        if (useQuickSort) SortUtils.quickSort(expenses, byDate);
        else SortUtils.mergeSort(expenses, byDate);
        sortedByDate = true;
    }

    public void sortByAmount(boolean useQuickSort) {
        Comparator<Expense> byAmount = Comparator.comparingDouble(Expense::getAmount);
        if (useQuickSort) SortUtils.quickSort(expenses, byAmount);
        else SortUtils.mergeSort(expenses, byAmount);
        sortedByDate = false;
    }

    public void sortByCategory(boolean useQuickSort) {
        Comparator<Expense> byCategory = Comparator.comparing(Expense::getCategory);
        if (useQuickSort) SortUtils.quickSort(expenses, byCategory);
        else SortUtils.mergeSort(expenses, byCategory);
        sortedByDate = false;
    }

    // ---------------------- SEARCHING ----------------------

    public List<Expense> searchByKeyword(String keyword) {
        return SearchUtils.linearSearchByKeyword(expenses, keyword);
    }

    /** Requires the list to be date-sorted first; sorts automatically if needed. */
    public List<Expense> searchByDate(LocalDate date) {
        if (!sortedByDate) {
            sortByDate(false);
        }
        return SearchUtils.allOnDate(expenses, date);
    }

    /** O(n) linear scan for a given month within a given year (Monthly view). */
    public List<Expense> getExpensesForMonth(int year, int month) {
        return SearchUtils.filterByYearMonth(expenses, year, month);
    }

    /** O(n) linear scan for an entire year (Yearly view). */
    public List<Expense> getExpensesForYear(int year) {
        return SearchUtils.filterByYearMonth(expenses, year, null);
    }

    /** O(n) linear scan summing everything logged today (Profile "daily expenses" stat). */
    public double getTodayTotal() {
        return SearchUtils.sumOnDate(expenses, LocalDate.now());
    }

    // ---------------------- HASHMAP AGGREGATION ----------------------

    public Map<String, Double> getCategoryTotals() {
        return new HashMap<>(categoryTotals);
    }

    public double getTotalSpent() {
        double total = 0;
        for (double v : categoryTotals.values()) total += v;
        return total;
    }

    // ---------------------- HEAP: TOP EXPENSES ----------------------

    public List<Expense> getTopExpenses(int n) {
        return topExpensesHeap.topN(n);
    }

    private void rebuildHeap() {
        while (!topExpensesHeap.isEmpty()) topExpensesHeap.extractMax();
        for (Expense e : expenses) topExpensesHeap.insert(e);
    }

    // ---------------------- LINKED LIST: ACTIVITY LOG ----------------------

    public List<String> getRecentActivity(int limit) {
        List<String> result = new ArrayList<>();
        int count = 0;
        for (String s : activityLog) {
            if (count++ >= limit) break;
            result.add(s);
        }
        return result;
    }

    // ---------------------- QUEUE: REMINDERS ----------------------

    public void scheduleReminder(String reminderText) {
        reminderQueue.enqueue(reminderText);
    }

    public String processNextReminder() {
        if (reminderQueue.isEmpty()) return null;
        return reminderQueue.dequeue();
    }

    public int pendingReminderCount() {
        return reminderQueue.size();
    }

    /** Wipes every expense and resets all structures (used by "Delete account"). */
    public void clearAll() {
        expenses.clear();
        idIndex.clear();
        categoryTotals.clear();
        while (!topExpensesHeap.isEmpty()) topExpensesHeap.extractMax();
        while (!reminderQueue.isEmpty()) reminderQueue.dequeue();
        while (!activityLog.isEmpty()) activityLog.removeFirst();
        nextId = 1;
        sortedByDate = false;
        persist();
    }

    // ---------------------- PERSISTENCE ----------------------

    private void loadFromDisk() {
        List<Expense> loaded = FileStorage.load(dataFile);
        for (Expense e : loaded) {
            expenses.add(e);
            idIndex.put(e.getId(), e);
            categoryTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
            topExpensesHeap.insert(e);
            if (e.getId() >= nextId) nextId = e.getId() + 1;
        }
    }

    private void persist() {
        FileStorage.save(dataFile, expenses);
    }
}
