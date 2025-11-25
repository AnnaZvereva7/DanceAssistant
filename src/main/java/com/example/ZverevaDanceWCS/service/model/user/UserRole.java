package com.example.ZverevaDanceWCS.service.model.user;

public enum UserRole {
    ADMIN,
    PERMANENT,
    NEW, //не прошло ни одного занятия
    GROUP,//занятия в группе или буткэмпы, мастерклассы
    OLD;//давно не занимались, все занятия оплачены
}

