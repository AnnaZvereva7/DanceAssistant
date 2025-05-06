package com.example.ZverevaDanceWCS.service;

import java.time.format.DateTimeFormatter;

public class Constant {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter formatterTimeFirst=DateTimeFormatter.ofPattern("HH:mm dd.MM");
    public static final DateTimeFormatter formatterJustDate=DateTimeFormatter.ofPattern("dd.MM.yy");

    public static final String adminTelegramName = "AnnaZverevaMorozova";
    public static final long adminChatId = 152942083;

    public static final int permanentStudentCost = 30;
    public static final int newStudentCost=40;

    public static final String CNR = "Command not recognized";

}
