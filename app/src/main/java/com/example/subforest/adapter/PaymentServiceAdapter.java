// package com.example.subforest.adapter;
package com.example.subforest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.serviceName.setText(payment.getServiceName());
        holder.amount.setText(String.format("%,d원", payment.getAmount()));
        holder.nextPaymentDate.setText(payment.getNextPaymentDate() + " 결제 예정");
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public void updateData(List<PaymentService> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, amount, nextPaymentDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            amount = itemView.findViewById(R.id.payment_amount);
            nextPaymentDate = itemView.findViewById(R.id.next_payment_date);
        }
    }
}