package com.example.ZverevaDanceWCS.service.model.lessons;

public enum LessonStatus {
    PENDING_STUDENT_CONFIRMATION, //создал тренер или расписание, стоимость не установлена
    PENDING_TRAINER_CONFIRMATION,//создал ученик, стоимость не установлена
    PLANNED, //подтвердила
    COMPLETED, //изменила статус у прошедшего занятия, добавлена стоимость в зависимости от пользовательской роли
    PAID, //прошла оплата
    CANCELED; //занятие было отменено
}
