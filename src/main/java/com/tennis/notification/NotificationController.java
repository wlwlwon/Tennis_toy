package com.tennis.notification;

import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;
    private final NotificationService service;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = repository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked,notifications.size());
        model.addAttribute("isNew",true);
        service.markAsRead(notifications);
        return "notification/list";

    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfChecked = repository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications,notifications.size(),numberOfChecked);
        model.addAttribute("isNew",false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account) {
        repository.deleteByAccountAndChecked(account,true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications, long numberOfChecked,long numberOfNotChecked) {
        List<Notification> newMoimNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingMoimNotifications = new ArrayList<>();

        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case Moim_CREATED: newMoimNotifications.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotifications.add(notification);break;
                case Moim_UPDATED: watchingMoimNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked",numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newMoimNotifications",newMoimNotifications);
        model.addAttribute("eventEnrollmentNotifications",eventEnrollmentNotifications);
        model.addAttribute("watchingMoimNotifications",watchingMoimNotifications);
    }

}
