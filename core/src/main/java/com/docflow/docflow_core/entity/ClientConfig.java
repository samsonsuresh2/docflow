package com.docflow.docflow_core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class ClientConfig {
    @Id
    @Column(name = "CLIENT_ID", nullable = false)
    private String clientId;
    private String idFormat;
    @Column(columnDefinition="TEXT")
    private String uploadFieldsJson;
    @Column(columnDefinition="TEXT")
    private String themeJson;
}
