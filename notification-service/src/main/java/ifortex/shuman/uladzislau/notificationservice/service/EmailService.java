package ifortex.shuman.uladzislau.notificationservice.service;

import ifortex.shuman.uladzislau.notificationservice.dto.EmailNotificationDto;

public interface EmailService {

  void sendEmail(EmailNotificationDto notificationDto);
}