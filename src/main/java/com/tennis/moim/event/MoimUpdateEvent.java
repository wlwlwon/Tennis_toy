package com.tennis.moim.event;

import com.tennis.moim.Moim;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MoimUpdateEvent {

    private final Moim moim;
    private final String message;
}
