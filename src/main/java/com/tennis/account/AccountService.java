package com.tennis.account;


import com.tennis.account.form.Notifications;
import com.tennis.account.form.Profile;
import com.tennis.account.form.SignUpForm;
import com.tennis.config.AppProperties;
import com.tennis.mail.EmailMessage;
import com.tennis.mail.EmailService;
import com.tennis.tag.Tag;
import com.tennis.zone.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));


        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();

        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {

        Context context = new Context();
        context.setVariable("link","/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickName",newAccount.getNickname());
        context.setVariable("linkName","이메일 인증하기");
        context.setVariable("message","니스테 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("니스테, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile,account);
        //modelMapper 로 생성자들 간결화
//        account.setUrl(profile.getUrl());
//        account.setOccupation(profile.getOccupation());
//        account.setLocation(profile.getLocation());
//        account.setBio(profile.getBio());
//        account.setProfileImage(profile.getProfileImage());
        accountRepository.save(account);//프로필 수정 처리 중요 transaction 상태인지 아닌지 관계 따지기
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications,account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {

        Context context = new Context();
        context.setVariable("link","/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickName",account.getNickname());
        context.setVariable("linkName","이메일로 로그인하기");
        context.setVariable("message","로그인하려면 아래 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("니스테, 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {

        //repositoy.getone() 필요한 순간에만 가져옴 lazyloading
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a-> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account){
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a ->a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (byNickname == null) {
            throw new IllegalArgumentException(byNickname + "에 해당하는 사용자가 없습니다.");
        }
        return byNickname;
    }
}
