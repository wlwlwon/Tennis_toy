package com.tennis.main;

import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentAccount Account account, HttpServletRequest req, RuntimeException e) {
        if(account !=null){
            log.info("'{}' requested '{}'",account.getNickname(),req.getRequestURI());
        }else{
            log.info("request '{}",req.getRequestURI());
        }
        log.error("bad request",e);
        return "error";
    }
}
