package com.example.expensetracker1.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.expensetracker1.data.AppDatabase;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.data.TransactionDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionDao transactionDao;
    private final LiveData<List<Transaction>> allTransactions;
    private final ExecutorService executorService;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        allTransactions = transactionDao.getAllTransactions();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpenses() {
        return transactionDao.getTotalExpenses();
    }

    public LiveData<Double> getTodayExpenses(long startOfDay, long endOfDay) {
        return transactionDao.getTodayExpenses(startOfDay, endOfDay);
    }

    public LiveData<Double> getMonthExpenses(long startOfMonth) {
        return transactionDao.getMonthExpenses(startOfMonth);
    }

    public LiveData<List<Transaction>> getTransactionsByDateRange(long start, long end) {
        // Method to facilitate better time-based filtering in ViewModel if needed
        return allTransactions; // Simple placeholder or implement DAO method if more scale is needed
    }

    public void insert(Transaction transaction) {
        executorService.execute(() -> transactionDao.insert(transaction));
    }

    public void update(Transaction transaction) {
        executorService.execute(() -> transactionDao.update(transaction));
    }

    public LiveData<Transaction> getTransactionById(int id) {
        return transactionDao.getTransactionById(id);
    }

    public void delete(Transaction transaction) {
        executorService.execute(() -> transactionDao.delete(transaction));
    }

    public void deleteAllTransactions() {
        executorService.execute(transactionDao::deleteAllTransactions);
    }

    public void deleteAllTransactions(Runnable onComplete) {
        executorService.execute(() -> {
            transactionDao.deleteAllTransactions();
            if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
            }
        });
    }
}
