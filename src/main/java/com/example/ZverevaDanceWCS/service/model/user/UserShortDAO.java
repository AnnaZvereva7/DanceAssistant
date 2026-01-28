package com.example.ZverevaDanceWCS.service.model.user;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class UserShortDAO {
    int id;
    String name;
    String chatName;
    UserRole role;
    String plans;
    int balance;

    public UserShortDAO (User user) {
        this.id=user.getId();
        this.name=user.getName();
        this.chatName=user.getChatName();
        this.role=user.getRole();
        this.plans= user.getAdditionalInfo();
        this.balance=user.getBalance();
    }

    @Override
    public String toString() {
        return id+"-"+name+" ("+chatName+") -"+role.toString();
    }

    public String stringPlans() {
        return id+"-"+name+": "+plans;
    }

    public String stringBalance() {
        return id+"-"+name+": "+balance;
    }
}
