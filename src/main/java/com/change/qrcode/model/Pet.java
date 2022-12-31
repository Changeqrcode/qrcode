package com.change.qrcode.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pet")
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(name = "text_content")
    private String textContent;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

}
