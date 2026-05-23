package com.example.expensetracker1;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.ItemTransactionBinding;
import com.example.expensetracker1.util.AppSettings;

import java.util.List;
import java.util.Objects;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Transaction transaction);
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        android.content.Context context = holder.itemView.getContext();
        
        holder.binding.tvTransactionTitle.setText(transaction.getTitle());
        holder.binding.tvTransactionCategory.setText(transaction.getCategory());

        boolean isExpense = Objects.equals(transaction.getType(), "EXPENSE");
        String prefix = isExpense ? "-" : "+";
        holder.binding.tvTransactionAmount.setText(prefix + AppSettings.formatAmount(context, transaction.getAmount()));

        int colorRes = isExpense ? R.color.error : R.color.secondary;
        holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, colorRes));

        int iconColorRes = isExpense ? R.color.error : R.color.secondary;
        holder.binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, iconColorRes));

        int bgColorRes = isExpense ? R.color.error_container : R.color.secondary_container;
        holder.binding.iconContainer.setCardBackgroundColor(ContextCompat.getColor(context, bgColorRes));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(transaction);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(transaction);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    public Transaction getTransactionAt(int position) {
        if (position >= 0 && position < transactions.size()) {
            return transactions.get(position);
        }
        return null;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        public TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
