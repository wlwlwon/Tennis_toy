package com.tennis.group.event;


import com.tennis.group.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GroupCreatedEvent{

    private final Group group;
}
