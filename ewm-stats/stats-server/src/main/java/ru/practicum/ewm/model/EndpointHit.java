package ru.practicum.ewm.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hits")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}