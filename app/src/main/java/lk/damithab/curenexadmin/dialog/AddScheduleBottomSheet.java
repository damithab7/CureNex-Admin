package lk.damithab.curenexadmin.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.AddScheduleBottomSheetBinding;
import lk.damithab.curenexadmin.model.TherapistSchedule;

public class AddScheduleBottomSheet extends BottomSheetDialogFragment {

    private OnScheduleAddedListener listener;

    private AddScheduleBottomSheetBinding binding;

    private String selectedTime;

    private Map<String, Integer> dayMap = new HashMap<>();

    private Spinner daySpinner;

    public interface OnScheduleAddedListener {
        void onAdded(TherapistSchedule schedule);
    }

    public AddScheduleBottomSheet(OnScheduleAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddScheduleBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnTime = binding.btnSelectTime;
        Button btnAdd = binding.btnAddSchedule;

        daySpinner = binding.daySpinner;

        List<String> weekDays = generateDayOfWeek();

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, weekDays);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        daySpinner.setAdapter(statusAdapter);


        btnTime.setOnClickListener(v1 -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(9)
                    .setMinute(0)
                    .setTitleText("Select Appointment Time")
                    .build();

            picker.show(getParentFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(v -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();

                String formattedTime = String.format("%02d:%02d", hour, minute);

                selectedTime = formattedTime;

                binding.addScheduleSelectedTime.setText(selectedTime);

            });
        });

        btnAdd.setOnClickListener(v -> {
            String maxAppoin = binding.addTherapistScheduleMaxappointments.getText().toString().trim();
            if(selectedTime == null){
                new ToastDialog(getParentFragmentManager(), "Please select a time before add");
                return;
            }

            int[] finaMaxAppointments = {0};
            try {
                finaMaxAppointments[0] = Integer.parseInt(maxAppoin);
            } catch (NumberFormatException e) {
                new ToastDialog(getParentFragmentManager(), "Invalid value for max appointments");
                return;
            }

            String scheduleId = String.valueOf(System.currentTimeMillis());

            int dayOfWeek = dayMap.get(daySpinner.getSelectedItem());

            TherapistSchedule schedule = TherapistSchedule.builder().scheduleId(scheduleId).status(Boolean.TRUE).dayOfWeek(dayOfWeek).startTime(selectedTime).maxAppointments(finaMaxAppointments[0]).build();
            listener.onAdded(schedule);
            dismiss();
        });
    }

    private List<String>generateDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        List<String> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String dayName = dayNumFormat.format(calendar.getTime());
            int dayInt = calendar.get(Calendar.DAY_OF_WEEK);

            dayMap.put(dayName, dayInt);

            days.add(dayName);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }
}
