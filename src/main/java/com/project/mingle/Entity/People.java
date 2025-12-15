package com.project.mingle.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "people")
public class People {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성
    private Long id;
    private String name;
}
