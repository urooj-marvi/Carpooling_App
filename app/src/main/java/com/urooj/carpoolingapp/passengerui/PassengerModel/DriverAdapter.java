package com.urooj.carpoolingapp.passengerui.PassengerModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.urooj.carpoolingapp.R;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private List<DriverData> driverList;

    // Constructor
    public DriverAdapter(List<DriverData> driverList) {
        this.driverList = driverList;
    }

    @Override
    public DriverViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_item, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DriverViewHolder holder, int position) {
        DriverData driver = driverList.get(position);
        holder.nameTextView.setText(driver.getName());
        holder.vehicleTextView.setText(driver.getVehicle());
        holder.routeTextView.setText(driver.getRoute());
        holder.availableImageView.setVisibility(driver.isAvailable() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    // ViewHolder class
    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, vehicleTextView, routeTextView;
        ImageView availableImageView;

        public DriverViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.driverName);
            vehicleTextView = itemView.findViewById(R.id.driverVehicle);
            routeTextView = itemView.findViewById(R.id.driverRoute);
            availableImageView = itemView.findViewById(R.id.driverAvailable);
        }
    }
}