package com.tennis.group.event;

import com.tennis.account.Account;
import com.tennis.account.AccountRepository;
import com.tennis.config.AppProperties;
import com.tennis.group.Group;
import com.tennis.group.GroupRepository;
import com.tennis.mail.EmailMessage;
import com.tennis.mail.EmailService;
import com.tennis.notification.Notification;
import com.tennis.notification.NotificationRepository;
import com.tennis.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Transactional
@Component
@Async
@RequiredArgsConstructor
public class GroupEventListener {

    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleGroupCreatedEvent(GroupCreatedEvent groupCreatedEvent){
        Group group = groupRepository.findGroupWithTagsAndZonesById(groupCreatedEvent.getGroup().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(group.getTags(), group.getZones()));
        accounts.forEach( account -> {
            if(account.isGroupCreatedByEmail()){
                sendGroupCreatedEmail(group, account,"새로운 스터디가 생겼습니다.","스터디올래,'"+group.getTitle()+"'스터디가 생겼습니다.");
            }
            if(account.isGroupCreatedByWeb()){
                createNotification(group, account, group.getShortDescription(), NotificationType.GROUP_UPDATED);
            }
        });
    }

    @EventListener
    public void handleGroupUpdateEvent(GroupUpdateEvent groupUpdateEvent) {
        Group group = groupRepository.findGroupWithManagersAndMembersById(groupUpdateEvent.getGroup().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(group.getManagers());
        accounts.addAll(group.getMembers());

        accounts.forEach(account -> {
            if (account.isGroupCreatedByEmail()) {
                sendGroupCreatedEmail(group,account,groupUpdateEvent.getMessage(),"스터디올래,'"+group.getTitle()+"'스터디에 새소식이 있습니다.");
            }
            if (account.isGroupCreatedByWeb()) {
                createNotification(group,account,group.getShortDescription(),NotificationType.GROUP_UPDATED);
            }
        });

    }

    private void createNotification(Group group, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(group.getTitle());
        notification.setLink("/group/"+ group.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendGroupCreatedEmail(Group group, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/group/"+ group.getEncodedPath());
        context.setVariable("linkName", group.getTitle());
        context.setVariable("message",contextMessage);
        context.setVariable("host",appProperties.getHost());


        String message = templateEngine.process("mail/simple-link",context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    ;

}
