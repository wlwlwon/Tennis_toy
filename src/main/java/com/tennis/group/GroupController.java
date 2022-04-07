package com.tennis.group;

import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
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
public class GroupController {

    private final GroupService groupService;
    private final ModelMapper modelMapper;
    private final GroupFormValidator groupFormValidator;
    private final GroupRepository groupRepository;



    @InitBinder("groupForm")
    public void groupFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(groupFormValidator);
    }

    //"/study/{path}을 통해 study그룹 divide
    @GetMapping("/study/{path}")
    public String viewGroup(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Group group = groupService.getGroup(path);
        model.addAttribute(account);
        model.addAttribute(group);
        return "group/view";
    }

    @GetMapping("/new-group")
    public String newGroupForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new GroupForm());
        return "group/form";
    }

    @PostMapping("/new-group")
    public String newGroupSubmit(@CurrentAccount Account account, @Valid GroupForm groupForm, Errors errors, Model model){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "group/form";
        }

        Group newGroup = groupService.createNewGroup(modelMapper.map(groupForm, Group.class), account);
        return "redirect:/group/" + URLEncoder.encode(newGroup.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/group/{path}/members")
    public String viewGroupMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Group study = groupService.getGroup(path);
        model.addAttribute(account);
        model.addAttribute(group);
        return "group/members";
    }

    @GetMapping("/group/{path}/join")
    public String joinGroup(@CurrentAccount Account account, @PathVariable String path) {
        Group group = groupRepository.findGroupWithMembersByPath(path);
        groupService.addMember(group,account);
        return "redirect:/group/" + group.getEncodedPath() + "/members";
    }
    @GetMapping("/group/{path}/leave")
    public String leaveGroup(@CurrentAccount Account account, @PathVariable String path) {
        Group group = groupRepository.findGroupWithMembersByPath(path);
        groupService.removeMember(group,account);
        return "redirect:/group/" + group.getEncodedPath() + "/members";
    }


    @GetMapping("/group/data")
    public String generateTestData(@CurrentAccount Account account) {
        groupService.generateTestGroup(account);
        return "redirect:/";
    }

}
