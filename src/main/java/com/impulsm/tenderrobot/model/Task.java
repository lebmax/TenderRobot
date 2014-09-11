package com.impulsm.tenderrobot.model;

import java.io.Serializable;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Task implements Serializable {

    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    private Date startTime;

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(targetEntity = TaskStatus.class)
    private TaskStatus status;

    @OneToOne(targetEntity = RequestType.class)
    private RequestType requestType;

    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    private Date beginDate;

    @OneToOne(targetEntity = AuctionType.class)
    private AuctionType auctionType;

    @Basic
    private String bid;

    @Column(nullable = false)
    @Basic
    private String url;

    public Task() {

    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getBeginDate() {
        return this.beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public AuctionType getAuctionType() {
        return this.auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public String getBid() {
        return this.bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
