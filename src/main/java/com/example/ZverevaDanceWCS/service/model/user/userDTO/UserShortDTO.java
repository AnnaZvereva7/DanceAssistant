package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDTO {
    private Long id;
    private String name;
    private String role;

    public static UserShortDTO toShortDTO(User user) {
        return new UserShortDTO(user.getId(), user.getName(), user.getRole().toString());
    }
}
