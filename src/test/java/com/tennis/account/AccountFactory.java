package com.tennis.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account jiwon = new Account();
        jiwon.setNickname(nickname);
        jiwon.setEmail(nickname+"@email.com");
        accountRepository.save(jiwon);
        return jiwon;
    }
}
