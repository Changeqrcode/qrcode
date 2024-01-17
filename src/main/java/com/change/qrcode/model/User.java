package com.change.qrcode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "util_user")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    private String username;

    
    @ManyToOne
    @JoinColumn(name="package_id")
    private Packages packages;
    
    private String password;

    private String email;
    @OneToMany(mappedBy="user")
    private Set<QR> QRS;
    @Column(name="resetPasswordToken")
    private String resetPasswordToken;

    @Column(name="package_enddate")
    private Date packageEndDate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))

    private Collection<Role> roles;

    public User(String username, String password, String email,Collection<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }
    public User( String username, String password, String email, Set<QR> QRS, String resetPasswordToken, Collection<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.QRS = QRS;
        this.resetPasswordToken = resetPasswordToken;
        this.roles = roles;
    }



}
