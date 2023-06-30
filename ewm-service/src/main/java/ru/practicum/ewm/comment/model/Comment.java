package ru.practicum.ewm.comment.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    Long id;

    @Column(name = "text", nullable = false)
    String text;

    @OneToOne
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(name = "comment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    CommentStatus status;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
}