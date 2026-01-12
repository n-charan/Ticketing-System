package com.example.ticketing.repository;

import com.example.ticketing.model.Ticket;
import com.example.ticketing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOwner(User owner);
    List<Ticket> findByAssignee(User assignee);
    List<Ticket> findByStatus(Ticket.Status status);
}
