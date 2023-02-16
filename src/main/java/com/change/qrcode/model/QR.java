package com.change.qrcode.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "qr")
@Getter
@Setter
public class QR {
    @Id
    @GeneratedValue()
    @Type(type="org.hibernate.type.UUIDCharType")
    private UUID id = UUID.randomUUID();

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "links")
    private String links;

    @Column(name = "is_recorded")
    private Boolean isRecorded;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy= "QR")
    private Set<UploadImage> images;
}
