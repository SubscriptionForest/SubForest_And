package com.example.subforest.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.subforest.R;
import com.example.subforest.network.ApiRepository;

public class SubscriptionAdapter extends ListAdapter<ApiRepository.SubscriptionItem, SubscriptionAdapter.VH> {

    public interface Listener {
        void onEdit(ApiRepository.SubscriptionItem item);
        void onDelete(ApiRepository.SubscriptionItem item);
        void onClick(ApiRepository.SubscriptionItem item);
    }

    private final Listener listener;

    public SubscriptionAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ApiRepository.SubscriptionItem> DIFF =
            new DiffUtil.ItemCallback<ApiRepository.SubscriptionItem>() {
                @Override public boolean areItemsTheSame(@NonNull ApiRepository.SubscriptionItem a,
                                                         @NonNull ApiRepository.SubscriptionItem b) {
                    return a.id == b.id;
                }
                @Override public boolean areContentsTheSame(@NonNull ApiRepository.SubscriptionItem a,
                                                            @NonNull ApiRepository.SubscriptionItem b) {
                    boolean logoEq = (a.logoUrl == null && b.logoUrl == null)
                            || (a.logoUrl != null && a.logoUrl.equals(b.logoUrl));
                    return a.name.equals(b.name)
                            && a.amount == b.amount
                            && a.repeatDays == b.repeatDays
                            && a.autoPayment == b.autoPayment
                            && a.shared == b.shared
                            && a.startDate.equals(b.startDate)
                            && logoEq;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_item, parent, false);
        return new VH(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ImageView logo;
        private final TextView name, period, amount;
        private final TextView autoDot, nextLabel, nextDateValue;
        private final ImageButton btnEdit, btnDelete;

        VH(@NonNull View itemView, Listener listener) {
            super(itemView);
            logo         = itemView.findViewById(R.id.logo);
            name         = itemView.findViewById(R.id.name);
            period       = itemView.findViewById(R.id.period);
            amount       = itemView.findViewById(R.id.amount);
            autoDot      = itemView.findViewById(R.id.autoDot);
            nextLabel    = itemView.findViewById(R.id.nextLabel);
            nextDateValue= itemView.findViewById(R.id.nextDateValue);
            btnEdit      = itemView.findViewById(R.id.btnEdit);
            btnDelete    = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                Object tag = itemView.getTag();
                if (listener != null && tag instanceof ApiRepository.SubscriptionItem)
                    listener.onClick((ApiRepository.SubscriptionItem) tag);
            });
            btnEdit.setOnClickListener(v -> {
                Object tag = itemView.getTag();
                if (listener != null && tag instanceof ApiRepository.SubscriptionItem)
                    listener.onEdit((ApiRepository.SubscriptionItem) tag);
            });
            btnDelete.setOnClickListener(v -> {
                Object tag = itemView.getTag();
                if (listener != null && tag instanceof ApiRepository.SubscriptionItem)
                    listener.onDelete((ApiRepository.SubscriptionItem) tag);
            });
        }

        void bind(ApiRepository.SubscriptionItem it) {
            itemView.setTag(it);

            name.setText(it.name);
            period.setText(DateUtils.periodLabel(it.startDate, it.repeatDays));
            String priceText = String.format("%,d원", it.amount);
            if (it.shared) {
                priceText += " (공유)";
            }
            amount.setText(priceText);

            if (it.autoPayment) {
                autoDot.setTextColor(Color.parseColor("#4CAF50")); // auto=초록
                nextLabel.setText("다음 결제일 : ");
            } else {
                autoDot.setTextColor(Color.parseColor("#F44336")); // non-auto=빨강
                nextLabel.setText("종료 예정일 : ");
            }

            String computed = com.example.subforest.ui.DateUtils.addDaysMd(it.startDate, it.repeatDays);
            nextDateValue.setText(computed);

            if (it.logoUrl != null && !it.logoUrl.isEmpty()) {
                Glide.with(logo.getContext()).load(it.logoUrl).into(logo);
            } else {
                logo.setImageResource(R.drawable.ic_subforest);
            }
        }
    }
}