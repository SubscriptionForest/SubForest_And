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
import com.example.subforest.model.SubscribedService;
import java.util.List;

public class SubscribedServiceAdapter extends RecyclerView.Adapter<SubscribedServiceAdapter.ViewHolder> {

    private List<SubscribedService> services;

    public SubscribedServiceAdapter(List<SubscribedService> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscribed_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 구독 서비스가 3개를 초과하고, 현재 아이템이 4번째 아이템일 경우
        if (services.size() > 3 && position == 3) {
            holder.serviceLogo.setVisibility(View.GONE); // 로고 숨기기
            holder.countText.setVisibility(View.VISIBLE); // 텍스트 보이기
            holder.countText.setText("+" + (services.size() - 3));
        } else {
            // 나머지 3개 로고 표시
            SubscribedService service = services.get(position);
            holder.serviceLogo.setVisibility(View.VISIBLE); // 로고 보이기
            holder.countText.setVisibility(View.GONE); // 텍스트 숨기기

            // Glide를 사용하여 로고 이미지 로드
            Glide.with(holder.itemView.getContext())
                    .load(service.getLogoUrl())
                    .placeholder(R.drawable.ic_subforest)
                    .into(holder.serviceLogo);
        }
    }

    @Override
    public int getItemCount() {
        // 구독 서비스가 3개 초과일 경우, 4개를 표시 (3개의 로고 + 1개의 "+N")
        if (services.size() > 3) {
            return 4;
        } else {
            // 3개 이하일 경우, 있는 만큼 표시
            return services.size();
        }
    }

    public void updateData(List<SubscribedService> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceLogo;
        TextView countText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceLogo = itemView.findViewById(R.id.service_logo);
            countText = itemView.findViewById(R.id.count_text);
        }
    }
}