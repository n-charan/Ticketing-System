package com.example.ticketing.controller;

import com.example.ticketing.model.Role;
import com.example.ticketing.model.Ticket;
import com.example.ticketing.model.User;
import com.example.ticketing.repository.TicketRepository;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public AdminController(UserService userService, UserRepository userRepository, TicketRepository ticketRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) return ResponseEntity.badRequest().body("exists");
        if (req.getRoles()==null || req.getRoles().isEmpty()) req.setRoles(Set.of(Role.ROLE_USER));
        return ResponseEntity.ok(userService.createUser(req));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return ResponseEntity.notFound().build();
        userService.deleteById(id);
        return ResponseEntity.ok("deleted");
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<?> setRoles(@PathVariable Long id, @RequestBody Set<Role> roles) {
        User u = userRepository.findById(id).orElseThrow();
        u.setRoles(roles);
        userRepository.save(u);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/tickets")
    public List<Ticket> allTickets() {
        return ticketRepository.findAll();
    }

    @PostMapping("/tickets/{id}/force-status")
    public ResponseEntity<?> forceStatus(@PathVariable Long id, @RequestParam Ticket.Status status) {
        Ticket t = ticketRepository.findById(id).orElseThrow();
        t.setStatus(status);
        ticketRepository.save(t);
        return ResponseEntity.ok(t);
    }
}
