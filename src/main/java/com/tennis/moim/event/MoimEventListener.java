package com.tennis.moim.event;

import com.tennis.account.Account;
import com.tennis.account.AccountPredicates;
import com.tennis.account.AccountRepository;
import com.tennis.config.AppProperties;
import com.tennis.moim.Moim;
import com.tennis.moim.MoimRepository;
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
public class MoimEventListener {

    private final MoimRepository moimRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleMoimCreatedEvent(MoimCreatedEvent moimCreatedEvent){
        Moim moim = moimRepository.findMoimWithTagsAndZonesById(moimCreatedEvent.getMoim().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(moim.getTags(), moim.getZones()));
        accounts.forEach( account -> {
            if(account.isMoimCreatedByEmail()){
                sendMoimCreatedEmail(moim, account,"새로운 스터디가 생겼습니다.","스터디올래,'"+moim.getTitle()+"'스터디가 생겼습니다.");
            }
            if(account.isMoimCreatedByWeb()){
                createNotification(moim, account, moim.getShortDescription(), NotificationType.Moim_UPDATED);
            }
        });
    }

    @EventListener
    public void handleMoimUpdateEvent(MoimUpdateEvent moimUpdateEvent) {
        Moim moim = moimRepository.findMoimWithManagersAndMembersById(moimUpdateEvent.getMoim().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(moim.getManagers());
        accounts.addAll(moim.getMembers());

        accounts.forEach(account -> {
            if (account.isMoimCreatedByEmail()) {
                sendMoimCreatedEmail(moim,account,moimUpdateEvent.getMessage(),"모임,'"+moim.getTitle()+"'모임에 새소식이 있습니다.");
            }
            if (account.isMoimCreatedByWeb()) {
                createNotification(moim,account,moim.getShortDescription(),NotificationType.Moim_UPDATED);
            }
        });

    }

    private void createNotification(Moim moim, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(moim.getTitle());
        notification.setLink("/moim/"+ moim.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendMoimCreatedEmail(Moim moim, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/moim/"+ moim.getEncodedPath());
        context.setVariable("linkName", moim.getTitle());
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
