package com.change.qrcode.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "upload_image")
@Getter
@Setter
public class UploadImage {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name="qr_id")
    private QR QR;
}
