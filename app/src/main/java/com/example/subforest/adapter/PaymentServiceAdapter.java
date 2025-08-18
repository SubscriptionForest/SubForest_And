package com.example.subforest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.subforest.R;
import com.example.subforest.model.PaymentService;
import java.util.List;

public class PaymentServiceAdapter extends RecyclerView.Adapter<PaymentServiceAdapter.ViewHolder> {
    private List<PaymentService> payments;

    public PaymentServiceAdapter(List<PaymentService> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentService payment = payments.get(position);

        // 1. 서비스 로고 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.getContext())
                .load(payment.getServiceLogoUrl())
                .placeholder(R.drawable.ic_subforest)
                .into(holder.serviceLogo);

        // 2. 남은 일수 아이콘 동적 설정
        int remainingDaysIcon = getRemainingDaysIcon(payment.getDaysLeft());
        holder.remainingDaysIcon.setImageResource(remainingDaysIcon);

        // 3. 텍스트 설정
        holder.serviceName.setText(payment.getServiceName());
        holder.amount.setText(String.format("%,d원", payment.getAmount()));
        holder.paymentCycle.setText(payment.getPaymentCycle());
        holder.nextPaymentDate.setText(payment.getDaysLeft() + " 일 남음");

        // 배경색을 동적으로 설정하는 코드는 제거됩니다.
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public void updateData(List<PaymentService> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    // 남은 일수에 따라 아이콘을 반환하는 메서드는 그대로 둡니다.
    private int getRemainingDaysIcon(int daysLeft) {
        if (daysLeft <= 3) {
            return R.drawable.ic_warning_red;
        } else if (daysLeft <= 7) {
            return R.drawable.ic_warning_yellow;
        } else {
            return R.drawable.ic_check_green;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceLogo, remainingDaysIcon;
        TextView serviceName, amount, paymentCycle, nextPaymentDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceLogo = itemView.findViewById(R.id.service_logo);
            remainingDaysIcon = itemView.findViewById(R.id.remaining_days_icon);
            serviceName = itemView.findViewById(R.id.service_name);
            amount = itemView.findViewById(R.id.payment_amount);
            paymentCycle = itemView.findViewById(R.id.payment_cycle);
            nextPaymentDate = itemView.findViewById(R.id.next_payment_date);
        }
    }
}