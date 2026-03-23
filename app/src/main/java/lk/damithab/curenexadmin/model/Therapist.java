package lk.damithab.curenexadmin.model;

import com.google.firebase.firestore.Exclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Therapist {
    private String therapistId;
    private String uid;
    private String name;
    @Getter(onMethod = @__({@Exclude}))
    @Setter(onMethod = @__({@Exclude}))
    private String serviceName;
    private long lastUpdate;
    private boolean status;

    private String documentId;
    private String serviceId;
    private String genderId;
    private String title; /// Dr. Mrs. Ms.
    private String bio;
    private double rate; /// Price per hour
    private float rating; /// Star Rating
    private String therapistImage;
    private String workEmail;
    private String workMobileNo;


}
