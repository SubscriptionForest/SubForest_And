package com.example.subforest;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private final List<Subscription> subscriptionList;

    public SubscriptionAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription item = subscriptionList.get(position);

        holder.serviceName.setText(item.getServiceName());
        holder.period.setText(item.getPeriod());
        holder.price.setText(item.getPrice());

        // 텍스트 내용 분기 처리
        if (item.isAutoPay()) {
            holder.statusText.setText("● 다음 결제일 : " + item.getDate());
            holder.statusText.setTextColor(Color.parseColor("#388E3C")); // 녹색 계열
        } else {
            holder.statusText.setText("● 종료 예정일 : " + item.getDate());
            holder.statusText.setTextColor(Color.parseColor("#D32F2F")); // 빨강 계열
        }

        // 로고 이미지 설정
        switch (item.getServiceName()) {
            case "YouTube":
                holder.logoIcon.setImageResource(R.drawable.logo_youtube);
                break;
            case "Spotify":
                //holder.logoIcon.setImageResource(R.drawable.logo_spotify);
                break;
            case "Netflix":
                //holder.logoIcon.setImageResource(R.drawable.logo_netflix);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, period, price, statusText;
        ImageView logoIcon, editBtn, deleteBtn;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            period = itemView.findViewById(R.id.period);
            price = itemView.findViewById(R.id.price);
            statusText = itemView.findViewById(R.id.statusText);
            logoIcon = itemView.findViewById(R.id.logoIcon);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}