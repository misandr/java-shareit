package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments", schema = "public")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(nullable = false, name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(nullable = false, name = "author_id")
    private User author;

    @Column(name = "created_date")
    private LocalDateTime created;
}
