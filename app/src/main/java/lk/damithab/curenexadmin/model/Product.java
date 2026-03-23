package lk.damithab.curenexadmin.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private List<String> images;
    private int stockCount;
    private boolean status;
    private float rating;
    private long lastUpdate;
    private List<Attribute> attribute;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        private String name;
        private String type;
        private List<String> values;
    }
}

