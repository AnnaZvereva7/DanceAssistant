package com.example.ZverevaDanceWCS.service.model.lessons;

public enum LessonStatus {
    TO_CONFIRM, //создал ученик, стоимость не установлена
    PLANNED, //подтвердила
    COMPLETED, //изменила статус у прошедшего занятия, добавлена стоимость в зависимости от пользовательской роли
    PAID, //прошла оплата
    CANCELED; //занятие было отменено
}
