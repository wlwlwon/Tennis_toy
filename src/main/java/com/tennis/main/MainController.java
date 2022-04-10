package com.tennis.main;

import com.tennis.account.Account;
import com.tennis.account.AccountRepository;
import com.tennis.account.CurrentAccount;
import com.tennis.event.EnrollmentRepository;
import com.tennis.moim.Moim;
import com.tennis.moim.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MoimRepository moimRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded,true));
            model.addAttribute("moimList",moimRepository.findByAccount(accountLoaded.getTags(),accountLoaded.getZones()));
            model.addAttribute("moimManagerOf",moimRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account,false));
            model.addAttribute("moimMemberOf", moimRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account,false));
            return "index-after-login";
        }

        model.addAttribute("moimList",moimRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true,false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/moim")
    public String searchMoim(@PageableDefault(size = 9,sort = "publishedDateTime",direction = Sort.Direction.ASC) Pageable pageable, String keyword, Model model) {
        Page<Moim> moimList = moimRepository.findByKeyword(keyword, pageable);
        model.addAttribute("moimPage",moimList);
        model.addAttribute("keyword",keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

}
