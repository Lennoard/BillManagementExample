package com.lennoardsilva.androidmobillschallenge.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.lennoardsilva.androidmobillschallenge.R;
import com.lennoardsilva.androidmobillschallenge.data.model.Despesa;
import com.lennoardsilva.androidmobillschallenge.data.model.Receita;
import com.lennoardsilva.androidmobillschallenge.data.model.Transaction;
import com.lennoardsilva.androidmobillschallenge.fragments.BaseListFragment;
import com.lennoardsilva.androidmobillschallenge.fragments.OnLoadMoreListener;
import com.lennoardsilva.androidmobillschallenge.sheets.ContextBottomSheet;
import com.lennoardsilva.androidmobillschallenge.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Keep
public class TransactionAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    private Activity activity;
    private List<Transaction> dataSet;
    private int visibleThreshold = 5;
    private boolean isLoading;
    private OnLoadMoreListener onLoadMoreListener;
    private CollectionReference ref;

    public TransactionAdapter(
            Activity activity,
            List<Transaction> list,
            RecyclerView recyclerView,
            CollectionReference ref
    ) {
        this.activity = activity;
        this.ref = ref;
        dataSet = list;

        final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();

                if (dy < 0 || totalItemCount < visibleThreshold) return;
                if (totalItemCount <= BaseListFragment.ITEMS_PER_PAGE) return;

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore(lastVisibleItemPosition);
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setRef(CollectionReference ref) {
        this.ref = ref;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.itemProgress);
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView description, date, value, attachments;
        ImageView statusIcon;
        View itemLayout;

        TransactionViewHolder(View v) {
            super(v);
            description = v.findViewById(R.id.transactionItemDescription);
            date = v.findViewById(R.id.transactionItemDate);
            attachments = v.findViewById(R.id.transactionItemAttachments);
            statusIcon = v.findViewById(R.id.transactionItemStatusIcon);
            value = v.findViewById(R.id.transactionItemValue);
            itemLayout = v.findViewById(R.id.transactionItemLayout);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
            vh = new TransactionViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_progress, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionViewHolder) {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            Transaction transaction = dataSet.get(position);

            transactionHolder.description.setText(transaction.getDescricao());
            transactionHolder.date.setText(Utils.dateMillisToString(transaction.getTime(), "HH:mm"));
            transactionHolder.value.setText(formatCurrency(transaction.getValor()));
            transactionHolder.attachments.setText(transaction.getAttachmentUrls().isEmpty()
                    ? activity.getString(R.string.no_attachment)
                    : activity.getString(R.string.attachments_format, transaction.getAttachmentUrls().size()
            ));

            Drawable attachmentDrawable = transaction.getAttachmentUrls().isEmpty()
                    ? activity.getDrawable(R.drawable.ic_no_attachment_18dp)
                    : activity.getDrawable(R.drawable.ic_attachment_18dp);

            transactionHolder.attachments.setCompoundDrawablesWithIntrinsicBounds(
                    attachmentDrawable,
                    null,
                    null,
                    null
            );

            if (transaction instanceof Despesa) {
                if (((Despesa) transaction).getPago()) {
                    transactionHolder.statusIcon.setImageDrawable(
                            activity.getDrawable(R.drawable.ic_paid)
                    );
                    transactionHolder.statusIcon.setColorFilter(
                            ContextCompat.getColor(activity, R.color.colorSuccess)
                    );
                } else {
                    transactionHolder.statusIcon.setImageDrawable(
                            activity.getDrawable(R.drawable.ic_pending_payment)
                    );
                    transactionHolder.statusIcon.setColorFilter(
                            ContextCompat.getColor(activity, R.color.colorWarning)
                    );
                }
            }

            if (transaction instanceof Receita) {
                if (((Receita) transaction).getRecebido()) {
                    transactionHolder.statusIcon.setImageDrawable(
                            activity.getDrawable(R.drawable.ic_received_payment)
                    );
                    transactionHolder.statusIcon.setColorFilter(
                            ContextCompat.getColor(activity, R.color.colorSuccess)
                    );
                } else {
                    transactionHolder.statusIcon.setImageDrawable(
                            activity.getDrawable(R.drawable.ic_pending_payment)
                    );
                    transactionHolder.statusIcon.setColorFilter(
                            ContextCompat.getColor(activity, R.color.colorWarning)
                    );
                }
            }

            transactionHolder.itemLayout.setOnClickListener(v -> {
                showOptionSheet(transaction, position);
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void addItem(@Nullable Transaction item) {
        dataSet.add(item);
        notifyItemInserted(dataSet.size());
    }

    public void clearItems() {
        dataSet.clear();
        notifyDataSetChanged();
    }

    public void setLoaded() {
        try {
            Transaction lastConfig = dataSet.get(dataSet.size() -1);
            if (lastConfig == null) {
                dataSet.remove(dataSet.size() -1);
                notifyItemRemoved(dataSet.size() -1);
            }
        } catch (Exception ignored) {}
        isLoading = false;
    }

    private void showOptionSheet(Transaction transaction, int position) {
        ArrayList<ContextBottomSheet.Option> options = new ArrayList<>();
        options.add(new ContextBottomSheet.Option(
                activity.getString(R.string.edit),
                "edit",
                R.color.colorOnBackground,
                R.drawable.ic_edit
        ));
        options.add(new ContextBottomSheet.Option(
                activity.getString(R.string.delete),
                "delete",
                R.color.colorError,
                R.drawable.ic_delete
        ));

        ContextBottomSheet sheet = ContextBottomSheet.newInstance(
                transaction.getDescricao(),
                options
        );

        sheet.setOnOptionClickListener(tag -> {
            switch (tag) {
                case "edit":
                    break;

                case "delete":
                    deleteTransaction(transaction, position, sheet);
                    break;
            }
        });

        sheet.show(((AppCompatActivity) activity).getSupportFragmentManager(), "ctxSheet");
    }

    private void deleteTransaction(Transaction transaction, int position, ContextBottomSheet sheet) {
        new AlertDialog.Builder(activity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.delete_prompt)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    ref.document(transaction.getId()).delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sheet.dismiss();
                            new Handler().postDelayed(() -> {
                                dataSet.remove(transaction);
                                notifyItemRemoved(position);
                            }, 200);
                        } else {
                            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> { })
                .show();
    }

    private String formatCurrency(Double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return activity.getString(R.string.value_format_list_item, formatter.format(value));
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }
}
