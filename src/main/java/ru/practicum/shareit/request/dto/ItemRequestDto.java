package ru.practicum.shareit.request.dto;

import lombok.*;

import java.sql.Timestamp;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ItemRequestDto {
    private long id;
    private String description;
    private Long requestorId;
    private Timestamp created;
}
