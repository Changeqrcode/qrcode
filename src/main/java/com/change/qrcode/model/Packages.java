package com.change.qrcode.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "packages")
@Getter
@Setter
public class Packages {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;


    @Column(name = "name")
    private String name;

    @Column(name = "character_limit")
    private Integer characterLimit;

    @Column(name = "link_limit")
    private Integer linkLimit;

    @Column(name = "image_limit")
    private Integer imageLimit;

    @Column(name = "logo_allowed")
    private Boolean logoAllowed;


    @Column(name = "location_allowed")
    private Boolean locationAllowed;

    @Column(name = "price")
    private Integer price;

    @Column(name = "year")
    private Integer year;

}
