package ru.practicum.shareit.request.dto;

import lombok.*;

import java.sql.Timestamp;

/**
 * TODO Sprint add-item-requests.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ItemRequestDto {
    long id;
    String description;
    Long requestorId;
    Timestamp created;
}
