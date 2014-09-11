package com.impulsm.tenderrobot.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public  class TaskStatus implements Serializable {


    @Column(nullable=false)
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;


    @Column(nullable=false)
    @Basic
    private String name;

    public TaskStatus(){

    }


   public Long getId() {
        return this.id;
    }


  public void setId (Long id) {
        this.id = id;
    }



   public String getName() {
        return this.name;
    }


  public void setName (String name) {
        this.name = name;
    }

}

