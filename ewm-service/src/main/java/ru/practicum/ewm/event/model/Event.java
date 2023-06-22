package ru.practicum.ewm.event.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    State state;

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

    @OneToOne
    @JoinColumn(name = "location_id", nullable = false)
    Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User initiator;

    //TODO
    /*
    Long confirmedRequests;
    Long views;
     */
}