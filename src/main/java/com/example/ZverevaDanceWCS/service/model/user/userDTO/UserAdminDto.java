package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String birthday;
    private Messenger messenger;
    private List<UserShortDTO> trainers;

    public UserAdminDto (User user, List<User> trainers) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.birthday = user.getBirthday() != null ? user.getBirthday().toString() : "";
        this.messenger = user.getMessenger();
        this.trainers = trainers.stream().map(UserShortDTO::toShortDTO).toList();
    }

}
