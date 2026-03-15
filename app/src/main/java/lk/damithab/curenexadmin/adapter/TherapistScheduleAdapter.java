package lk.damithab.curenexadmin.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.model.TherapistSchedule;
import lk.damithab.curenexadmin.module.GlideApp;

public class TherapistScheduleAdapter extends RecyclerView.Adapter<TherapistScheduleAdapter.ViewHolder> {
    private List<TherapistSchedule> scheduleList;

    private FirebaseStorage storage;

    private OnRemoveListener removeListener;

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setScheduleList(List<TherapistSchedule> scheduleList) {
        this.scheduleList = scheduleList;
        notifyDataSetChanged();
    }

    public void handleItemRemoved(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_therapist_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getAbsoluteAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (!scheduleList.isEmpty()) {

            TherapistSchedule schedule = scheduleList.get(position);

            holder.scheduleTime.setText(schedule.getStartTime());
            holder.scheduleDay.setText(getDayOfWeek(schedule.getDayOfWeek()));
            holder.scheduleMaxAppointments.setText(String.valueOf(schedule.getMaxAppointments()));

            holder.removeBtn.setOnClickListener(v -> {
                int pos = holder.getAbsoluteAdapterPosition();
                Log.i("Position", String.valueOf(pos));
                if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoved(currentPosition);
                }
            });

        }
    }

    private String getDayOfWeek(int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        List<String> days = new ArrayList<>();
        return dayNumFormat.format(calendar.get(dayOfWeek));
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView scheduleDay, scheduleTime, scheduleMaxAppointments;

        MaterialButton removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.scheduleDay = itemView.findViewById(R.id.item_therapist_schedule_day);
            this.scheduleMaxAppointments = itemView.findViewById(R.id.item_therapist_schedule_maxappointments);
            this.scheduleTime = itemView.findViewById(R.id.item_therapist_schedule_starttime);
            this.removeBtn = itemView.findViewById(R.id.item_therapist_schedule_remove);
        }
    }


    public interface OnRemoveListener {
        void onRemoved(int position);
    }

}