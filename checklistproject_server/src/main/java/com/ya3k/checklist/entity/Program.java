package com.ya3k.checklist.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "programs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Program {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
    @Column(name="status")
    private String status;
    @Column(name="create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    private LocalDateTime create_time;

    @Column(name="end_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("endtime")
    private Date end_time;




}
