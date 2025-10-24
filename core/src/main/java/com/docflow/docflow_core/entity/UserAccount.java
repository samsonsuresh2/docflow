package com.docflow.docflow_core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String username;
    @Column(nullable=false)
    private String passwordHash;
    private String clientId;
    private String role;
    private boolean active;
}
