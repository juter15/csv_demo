package com.example.csv.csv_demo;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvModel {
    @CsvBindByName
    private String name;
    @CsvBindByName
    private String age;
    @CsvBindByName
    private String grade;
    @CsvBindByName
    private String title;
    @CsvBindByName
    private String address;

    private float latitude;
    private float longitude;
}
