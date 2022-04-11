package com.tennis.moim;

import com.tennis.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoimFactory {

    @Autowired MoimService moimService;
    @Autowired MoimRepository moimRepository;

    public Moim createMoim(String path, Account manager) {
        Moim moim = new Moim();
        moim.setPath(path);
        moimService.createNewMoim(moim,manager);
        return moim;
    }
}
