package com.example.ssii.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;


@Entity
public class Projet {
    @Id
    private Integer code;
    private String nom;
    private LocalDate debut;
    private LocalDate fin;

    @OneToMany(mappedBy = "projet")
    private Set<Participation> participations;
}



