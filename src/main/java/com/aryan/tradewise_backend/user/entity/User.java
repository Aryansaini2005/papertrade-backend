package com.aryan.tradewise_backend.user.entity;

import com.aryan.tradewise_backend.user.enums.Provider;
import com.aryan.tradewise_backend.user.enums.Role;
import com.aryan.tradewise_backend.user.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    private String lastName;
    @Column(unique=true)
    private String email;

    @Column(nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable=false,updatable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false,updatable=false)
    private LocalDateTime updatedAt;

}
