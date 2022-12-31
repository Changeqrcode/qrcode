package com.change.qrcode.model;

import jakarta.persistence.*;

@Entity
@Table(name = "util_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;

    private String password;
}
