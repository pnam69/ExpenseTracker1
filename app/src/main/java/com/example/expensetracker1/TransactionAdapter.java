package com.example.expensetracker1;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.ItemTransactionBinding;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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
        holder.binding.tvTransactionTitle.setText(transaction.getTitle());
        holder.binding.tvTransactionCategory.setText(transaction.getCategory());

        boolean isExpense = Objects.equals(transaction.getType(), "EXPENSE");
        String prefix = isExpense ? "-" : "+";
        holder.binding.tvTransactionAmount.setText(prefix + String.format(Locale.getDefault(), "%,.0fđ", transaction.getAmount()));

        int colorRes = isExpense ? R.color.error : R.color.secondary;
        holder.binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));

        int iconColorRes = isExpense ? R.color.error : R.color.secondary;
        holder.binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), iconColorRes));

        int bgColorRes = isExpense ? R.color.error_container : R.color.secondary_container;
        holder.binding.iconContainer.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), bgColorRes));

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

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        public TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
