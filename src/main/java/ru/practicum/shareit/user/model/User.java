package ru.practicum.shareit.user.model;

import lombok.*;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class User {
    Long id;
    String name;
    String email;
}
