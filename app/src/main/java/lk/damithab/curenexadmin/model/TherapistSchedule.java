package lk.damithab.curenexadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TherapistSchedule {
    private String scheduleId;
    private int dayOfWeek;
    private String startTime;
    private int maxAppointments; ///Per Slot
    private boolean status;
}
