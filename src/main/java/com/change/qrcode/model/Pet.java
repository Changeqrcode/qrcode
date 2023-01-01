package com.change.qrcode.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pet")
@Getter
@Setter
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "is_recorded")
    private Boolean isRecorded;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

}
