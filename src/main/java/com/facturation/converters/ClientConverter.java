package com.facturation.converters;

import com.facturation.entities.Client;
import com.facturation.services.ClientService;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

@Named
@ApplicationScoped
@FacesConverter(value = "clientConverter", managed = true)
public class ClientConverter implements Converter<Client> {
    
    @EJB
    private ClientService clientService;
    
    @Override
    public Client getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            Long id = Long.valueOf(value);
            return clientService.trouverParId(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component, Client client) {
        if (client == null) {
            return "";
        }
        return String.valueOf(client.getId());
    }
}
