package com.example.ticketing.service;

import com.example.ticketing.model.Ticket;
import com.example.ticketing.model.User;

import java.util.List;
import java.util.Optional;

public interface TicketService {
    Ticket create(Ticket ticket);
    Optional<Ticket> findById(Long id);
    List<Ticket> findByOwner(User owner);
    List<Ticket> findAll();
    Ticket save(Ticket ticket);
}
