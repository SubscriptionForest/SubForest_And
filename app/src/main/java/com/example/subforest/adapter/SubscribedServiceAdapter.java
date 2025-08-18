// package com.example.subforest.adapter;
package com.example.subforest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        SubscribedService service = services.get(position);
        holder.serviceName.setText(service.getName());
        // TODO: 로고 이미지를 로드하는 코드 추가 (Glide 또는 Picasso 라이브러리 사용 권장)
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateData(List<SubscribedService> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
        }
    }
}