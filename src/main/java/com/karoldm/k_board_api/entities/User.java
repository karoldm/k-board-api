package com.karoldm.k_board_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name="users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Email cannot be null")
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    private String password;

    private String photoUrl;
    private LocalDate createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<Project> participatedProjects = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Task> tasks;

    public void addProjectParticipated(Project project) {
        participatedProjects.add(project);
    }

    public void removeProjectParticipated(Project project) {
        participatedProjects.remove(project);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
