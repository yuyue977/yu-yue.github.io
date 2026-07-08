package com.example.xiamenbackground.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "AdminManager")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 3)
    private String name;

    @Column(name = "Role", nullable = false, length = 10)
    private String role;

    @Column(name = "Telephone", nullable = false, length = 20)
    private String telephone;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 100)
    private String password;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
