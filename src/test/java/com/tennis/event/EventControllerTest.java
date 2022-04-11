package com.tennis.event;

import com.tennis.account.Account;
import com.tennis.account.AccountFactory;
import com.tennis.account.AccountRepository;
import com.tennis.account.WithAccount;
import com.tennis.infra.ContainerBaseTest;
import com.tennis.infra.MockMvcTest;
import com.tennis.moim.Moim;
import com.tennis.moim.MoimFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest extends ContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired MoimFactory moimFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("keesun")
    void newEnrollment_to_FCFS_event_accepted() throws Exception{
        Account whiteship = accountFactory.createAccount("whiteship");
        Moim moim = moimFactory.createMoim("test-moim", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, moim, whiteship);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event,may);
        eventService.newEnrollment(event,june);

        mockMvc.perform(post("/moim/" + moim.getPath() + "/events/"+ event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moim/" + moim.getPath() + "/events/" + event.getId()));

        Account keesun = accountRepository.findByNickname("keesun");
        isAccepted(keesun,event);
    }


    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중")
    @WithAccount("keesun")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception{
        Account whiteship = accountFactory.createAccount("whiteship");
        Moim moim = moimFactory.createMoim("test-moim", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, moim, whiteship);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event,may);
        eventService.newEnrollment(event,june);

        mockMvc.perform(post("/moim/" + moim.getPath() + "/events/"+ event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moim/" + moim.getPath() + "/events/" + event.getId()));

        Account keesun = accountRepository.findByNickname("keesun");
        isNotAccepted(keesun,event);
    }

    private void isNotAccepted(Account keesun, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event,keesun).isAccepted());
    }


    private void isAccepted(Account keesun, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event,keesun).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Moim moim, Account account) {
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event,moim,account);
    }
}