package ru.practicum.ewm.event.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "annotation", nullable = false)
    String annotation;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "event_state", nullable = false)
    @Enumerated(EnumType.STRING)
    ru.practicum.ewm.event.model.State state;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "paid", nullable = false)
    Boolean paid;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "participant_limit", nullable = false)
    Long participantLimit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    ru.practicum.ewm.event.model.Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    List<Request> participationRequests;

    @ManyToMany(mappedBy = "events")
    Set<Compilation> compilations;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    List<Comment> comments;
}