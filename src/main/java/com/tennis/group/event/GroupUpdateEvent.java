package com.tennis.group.event;

import com.tennis.group.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GroupUpdateEvent {

    private final Group group;
    private final String message;
}
