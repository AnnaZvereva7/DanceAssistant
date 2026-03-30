package com.example.ZverevaDanceWCS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication

public class   ZverevaDanceWcsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZverevaDanceWcsApplication.class, args);
	}

}

//todo Telegram  оставить один канал для учеников и тернеров, как прсоединиться к телеграмм с сайта
// (для уведомлений:изменения в уроках, доп.информация для ученика, информация об оплате- и просмотра расписания)
//todo интеграция с платежной системой
//todo whatsApap buiseness api для уведомлений

//todo номер wsdc учеников
//todo список ивентов с датами и прикрепить к ученикам, ссылки на мероприятия, результаты учеников, напоминания о мероприятиях за неделю со списком учеников
//todo история по каждому ученику (рекапы, планы, ивенты, номер wsdc, результаты)