package com.jlu.registration.module.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registration_windows",
        uniqueConstraints = @UniqueConstraint(columnNames = "semester"))
@Getter
@Setter
@NoArgsConstructor
public class RegistrationWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String semester;

    @Column(nullable = false)
    private boolean open = true;

    public RegistrationWindow(String semester) {
        this.semester = semester;
    }
}

