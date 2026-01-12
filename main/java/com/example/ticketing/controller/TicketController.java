package com.example.ticketing.controller;

import com.example.ticketing.model.Comment;
import com.example.ticketing.model.Ticket;
import com.example.ticketing.model.User;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.service.TicketService;
import com.example.ticketing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserService userService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserService userService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Ticket req, Authentication auth) {
        User owner = userService.findByUsername(auth.getName()).orElseThrow();
        Ticket t = Ticket.builder()
                .subject(req.getSubject())
                .description(req.getDescription())
                .priority(req.getPriority())
                .owner(owner)
                .build();
        return ResponseEntity.ok(ticketService.create(t));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> myTickets(Authentication auth) {
        User owner = userService.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(ticketService.findByOwner(owner));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Authentication auth) {
        Optional<Ticket> ot = ticketService.findById(id);
        if (ot.isEmpty()) return ResponseEntity.notFound().build();
        Ticket t = ot.get();
        boolean isAdminOrSupport = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPPORT"));
        boolean isOwner = t.getOwner().getUsername().equals(auth.getName());
        boolean isAssignee = t.getAssignee() != null && t.getAssignee().getUsername().equals(auth.getName());
        if (!(isAdminOrSupport || isOwner || isAssignee)) return ResponseEntity.status(403).body("forbidden");
        return ResponseEntity.ok(t);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Comment req, Authentication auth) {
        Ticket t = ticketService.findById(id).orElseThrow();
        User u = userService.findByUsername(auth.getName()).orElseThrow();
        boolean isAdminOrSupport = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPPORT"));
        boolean allowed = isAdminOrSupport || t.getOwner().getId().equals(u.getId()) || (t.getAssignee()!=null && t.getAssignee().getId().equals(u.getId()));
        if (!allowed) return ResponseEntity.status(403).body("forbidden");
        Comment c = Comment.builder().text(req.getText()).author(u).createdAt(Instant.now()).build();
        t.getComments().add(c);
        return ResponseEntity.ok(ticketService.save(t));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable Long id, @RequestParam Long assigneeId, Authentication auth) {
        Ticket t = ticketService.findById(id).orElseThrow();
        User actor = userService.findByUsername(auth.getName()).orElseThrow();
        User assignee = userRepository.findById(assigneeId).orElseThrow();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSupport = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORT"));
        boolean isOwner = t.getOwner().getId().equals(actor.getId());
        if (!(isAdmin || isSupport || isOwner)) return ResponseEntity.status(403).body("forbidden");

        t.setAssignee(assignee);
        return ResponseEntity.ok(ticketService.save(t));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam Ticket.Status status, Authentication auth) {
        Ticket t = ticketService.findById(id).orElseThrow();
        User actor = userService.findByUsername(auth.getName()).orElseThrow();
        boolean canChange = actor.getId().equals(t.getOwner().getId()) ||
                (t.getAssignee() != null && actor.getId().equals(t.getAssignee().getId())) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPPORT"));
        if (!canChange) return ResponseEntity.status(403).body("forbidden");
        t.setStatus(status);
        return ResponseEntity.ok(ticketService.save(t));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) Ticket.Status status,
                                    @RequestParam(required = false) Ticket.Priority priority,
                                    @RequestParam(required = false) Long assigneeId,
                                    @RequestParam(required = false) Long ownerId) {
        return ResponseEntity.ok(
                ticketService.findAll().stream().filter(t ->
                        (status == null || t.getStatus() == status) &&
                        (priority == null || t.getPriority() == priority) &&
                        (assigneeId == null || (t.getAssignee()!=null && t.getAssignee().getId().equals(assigneeId))) &&
                        (ownerId == null || (t.getOwner()!=null && t.getOwner().getId().equals(ownerId)))
                ).toList()
        );
    }
}
