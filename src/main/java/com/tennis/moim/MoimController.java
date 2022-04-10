package com.tennis.moim;

import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import com.tennis.moim.form.MoimForm;
import com.tennis.moim.validator.MoimFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class MoimController {

    private final MoimService moimService;
    private final ModelMapper modelMapper;
    private final MoimFormValidator moimFormValidator;
    private final MoimRepository moimRepository;



    @InitBinder("moimForm")
    public void moimFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(moimFormValidator);
    }

    //"/moim/{path}을 통해 moim그룹 divide
    @GetMapping("/moim/{path}")
    public String viewMoim(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoim(path);
        model.addAttribute(account);
        model.addAttribute(moim);
        return "moim/view";
    }

    @GetMapping("/new-moim")
    public String newMoimForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new MoimForm());
        return "moim/form";
    }

    @PostMapping("/new-moim")
    public String newMoimSubmit(@CurrentAccount Account account, @Valid MoimForm moimForm, Errors errors, Model model){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "moim/form";
        }

        Moim newMoim = moimService.createNewMoim(modelMapper.map(moimForm, Moim.class), account);
        return "redirect:/moim/" + URLEncoder.encode(newMoim.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/moim/{path}/members")
    public String viewMoimMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoim(path);
        model.addAttribute(account);
        model.addAttribute(moim);
        return "moim/members";
    }

    @GetMapping("/moim/{path}/join")
    public String joinMoim(@CurrentAccount Account account, @PathVariable String path) {
        Moim moim = moimRepository.findMoimWithMembersByPath(path);
        moimService.addMember(moim,account);
        return "redirect:/moim/" + moim.getEncodedPath() + "/members";
    }
    @GetMapping("/moim/{path}/leave")
    public String leaveMoim(@CurrentAccount Account account, @PathVariable String path) {
        Moim moim = moimRepository.findMoimWithMembersByPath(path);
        moimService.removeMember(moim,account);
        return "redirect:/moim/" + moim.getEncodedPath() + "/members";
    }


    @GetMapping("/moim/data")
    public String generateTestData(@CurrentAccount Account account) {
        moimService.generateTestMoim(account);
        return "redirect:/";
    }

}
