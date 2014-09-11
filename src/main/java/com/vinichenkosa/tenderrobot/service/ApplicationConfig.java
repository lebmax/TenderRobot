package com.vinichenkosa.tenderrobot.service;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.vinichenkosa.tenderrobot.service.AuctionTypeFacadeREST.class);
        resources.add(com.vinichenkosa.tenderrobot.service.RequestTypeFacadeREST.class);
        resources.add(com.vinichenkosa.tenderrobot.service.TaskFacadeREST.class);
        resources.add(com.vinichenkosa.tenderrobot.service.TaskStatusFacadeREST.class);
    }
    
}
