package ru.practicum.shareit.user.model;

import lombok.*;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class User {
    private Long id;
    private String name;
    private String email;
}
