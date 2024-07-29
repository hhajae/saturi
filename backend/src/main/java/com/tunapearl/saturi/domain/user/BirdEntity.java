package com.tunapearl.saturi.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "bird")
@ToString
public class BirdEntity {

    @Id @GeneratedValue
    @Column(name = "bird_id")
    private Long id;

    private String name;

    private String description;

    private String imagePath;

//    @OneToMany(mappedBy = "bird")
//    private List<UserEntity> users = new ArrayList<>();
}