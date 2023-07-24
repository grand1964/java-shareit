package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class UserDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}
