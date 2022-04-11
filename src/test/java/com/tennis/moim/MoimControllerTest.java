package com.tennis.moim;


import com.tennis.account.Account;
import com.tennis.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class MoimControllerTest {


    @Autowired protected MockMvc mockMvc;
    @Autowired protected MoimService moimService;
    @Autowired protected MoimRepository moimRepository;
    @Autowired protected AccountRepository accountRepository;

    @Test
    @WithAccount("jiwon")
    @DisplayName("모임 개설 폼 조회")
    void createMoimForm() throws Exception {
        mockMvc.perform(get("/new-moim"))
                .andExpect(status().isOk())
                .andExpect(view().name("moim/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeDoesNotExist("moimForm"));

    }

    @Test
    @WithAccount("jiwon")
    @DisplayName("모임 개설-완료")
    void createMoim_success() throws Exception {
        mockMvc.perform(post("/new-moim")
                .param("path","test-path")
                .param("title","moim title")
                .param("shortDescription", "short description of a moim")
                .param("fullDescription","full description of a moim")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moim/test-path"));

        Moim moim = moimRepository.findByPath("test-path");
        assertNotNull(moim);
        Account account = accountRepository.findByNickname("jiwon");
        assertTrue(moim.getManagers().contains(account));

    }

    @Test
    @WithAccount("jiwon")
    @DisplayName("모임 개설-실패")
    void createMoim_fail() throws Exception {
        mockMvc.perform(post("/new-moim")
                        .param("path","wrong-path")
                        .param("title","moim title")
                        .param("shortDescription", "short description of a moim")
                        .param("fullDescription","full description of a moim")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("moim/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("moimForm"))
                .andExpect(model().attributeExists("account"));

        Moim moim = moimRepository.findByPath("test-path");
        assertNull(moim);

    }

}
