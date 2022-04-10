package com.tennis.account.validator;


import com.tennis.account.Account;
import com.tennis.account.AccountRepository;
import com.tennis.account.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;
    @Override
    public boolean supports(Class<?> aClass) {
        return NicknameForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) o;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());
        if(byNickname!=null){
            errors.rejectValue("nickname","wrong.value","입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}