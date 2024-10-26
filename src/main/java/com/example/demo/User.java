package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * @author chenzhenyang
 * @date 2024/10/26 上午8:43
 */


@Entity(name = "uzer")
public class User {

    @Id
    private Long id;

    private String name;
    private String email;
    private Integer age;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
