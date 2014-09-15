package com.vinichenkosa.tenderrobot.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
@NamedQuery(name = "TaskStatus.findByCode", query = "select s from TaskStatus s where s.code = :code")
})
public class TaskStatus implements Serializable {

    @Column(unique = true, nullable = false)
    @Basic
    private int code;

    @Column(nullable = false)
    @Basic
    private String name;

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public TaskStatus() {

    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
