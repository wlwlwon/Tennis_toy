package com.tennis.main;

import com.tennis.account.Account;
import com.tennis.account.AccountRepository;
import com.tennis.account.CurrentAccount;
import com.tennis.event.EnrollmentRepository;
import com.tennis.group.Group;
import com.tennis.group.GroupRepository;
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

    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded,true));
            model.addAttribute("groupList",groupRepository.findByAccount(accountLoaded.getTags(),accountLoaded.getZones()));
            model.addAttribute("groupManagerOf",groupRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account,false));
            model.addAttribute("groupMemberOf", groupRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account,false));
            return "index-after-login";
        }

        model.addAttribute("groupList",groupRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true,false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/group")
    public String searchGroup(@PageableDefault(size = 9,sort = "publishedDateTime",direction = Sort.Direction.ASC) Pageable pageable, String keyword, Model model) {
        Page<Group> groupList = groupRepository.findByKeyword(keyword, pageable);
        model.addAttribute("groupPage",groupList);
        model.addAttribute("keyword",keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

}
