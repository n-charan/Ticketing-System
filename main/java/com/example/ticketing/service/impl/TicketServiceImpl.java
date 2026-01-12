package com.example.ticketing.service.impl;

import com.example.ticketing.model.Ticket;
import com.example.ticketing.model.User;
import com.example.ticketing.repository.TicketRepository;
import com.example.ticketing.service.TicketService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Ticket create(Ticket ticket) {
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        ticket.setStatus(Ticket.Status.OPEN);
        return ticketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> findByOwner(User owner) {
        return ticketRepository.findByOwner(owner);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket save(Ticket ticket) {
        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }
}
