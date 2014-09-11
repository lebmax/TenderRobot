/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.impulsm.tenderrobot.service;

import com.impulsm.tenderrobot.model.AuctionType;
import com.impulsm.tenderrobot.model.RequestType;
import com.impulsm.tenderrobot.model.Task;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author vinichenkosa
 */
@Stateless
@Path("task")
public class TaskFacadeREST extends AbstractFacade<Task> {

    @PersistenceContext(unitName = "TenderRobotPU")
    private EntityManager em;
    @Inject
    private RequestTypeFacadeREST requests;
    @Inject
    private AuctionTypeFacadeREST auctions;

    public TaskFacadeREST() {
        super(Task.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Task entity) {
        RequestType request = requests.find(entity.getRequestType().getId());
        AuctionType auction = auctions.find(entity.getAuctionType().getId());
        entity.setAuctionType(auction);
        entity.setRequestType(request);
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Long id, Task entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Long id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Task find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Task> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Task> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
