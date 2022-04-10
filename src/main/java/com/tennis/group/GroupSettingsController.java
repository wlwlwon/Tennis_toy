package com.tennis.group;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import com.tennis.group.form.GroupDescriptionForm;
import com.tennis.tag.Tag;
import com.tennis.tag.TagForm;
import com.tennis.tag.TagRepository;
import com.tennis.tag.TagService;
import com.tennis.zone.Zone;
import com.tennis.zone.ZoneForm;
import com.tennis.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/group/{path}/settings")
@RequiredArgsConstructor
public class GroupSettingsController {

    private final GroupService groupService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;


    @GetMapping("/tags")
    public String groupTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Group group = groupService.getGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(group);


        model.addAttribute("tags", group.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTagTitles));

        return "group/settings/tags";
    }

    @PostMapping( "/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm, @PathVariable String path){
        Group group = groupService.getGroupToUpdateTag(account, path);
        //Group group = groupService.getGroupToUpdate(account,path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        groupService.addTag(group,tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm,@PathVariable String path){
        Group group = groupService.getGroupToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        groupService.removeTag(group,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String groupZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Group group = groupService.getGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(group);

        model.addAttribute("zones",group.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));
        return "group/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm, @PathVariable String path){
        Group group = groupService.getGroupToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){
            return ResponseEntity.badRequest().build();
        }
        groupService.addZone(group,zone);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm,@PathVariable String path){
        Group group = groupService.getGroupToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){
            return ResponseEntity.badRequest().build();
        }
        groupService.removeZone(group,zone);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/banner")
    public String groupImageForm(@CurrentAccount Account account, @PathVariable String path, Model model){
        Group group = groupService.getGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(group);
        return "group/settings/banner";
    }
    @PostMapping("/banner")
    public String groupImageSubmit(@CurrentAccount Account account, @PathVariable String path,
                                   Model model, Errors errors, RedirectAttributes attributes, String image) {
        Group group = groupService.getGroupToUpdate(account, path);
        groupService.updateGroupImage(group,image);
        attributes.addFlashAttribute("message", "모임 이미지를 수정했습니다.");
        return "redirect:/group/" + group.getEncodedPath() +"/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableGroupBanner(@CurrentAccount Account account, @PathVariable String path) {
        Group group = groupService.getGroupToUpdate(account, path);
        groupService.enableGroupBanner(group);
        return "redirect:/group/" + group.getEncodedPath() + "/settings/banner";
    }
    @PostMapping("/banner/disable")
    public String disableGroupBanner(@CurrentAccount Account account, @PathVariable String path) {
        Group group = groupService.getGroupToUpdate(account, path);
        groupService.disableGroupBanner(group);
        return "redirect:/group/" + group.getEncodedPath() + "/settings/banner";
    }

    @GetMapping("/description")
    public String viewGroupSetting(@CurrentAccount Account account,@PathVariable String path, Model model) {
        Group group = groupService.getGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(group);
        model.addAttribute(modelMapper.map(group, GroupDescriptionForm.class));
        return "group/settings/description";

    }

    @PostMapping("/description")
    public String updateGroupInfo(@CurrentAccount Account account, @PathVariable String path, @Valid GroupDescriptionForm groupDescriptionForm,
                                  Errors errors, Model model, RedirectAttributes redirectAttributes) {
        Group group = groupService.getGroupToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(group);
            return "group/settings/description";
        }
        groupService.updateGroupDescription(group,groupDescriptionForm);
        redirectAttributes.addFlashAttribute("message", "모임 소개를 수정했습니다.");
        return "redirect:/group/" + group.getEncodedPath() +"/settings/description";
    }

    @GetMapping("/group")
    public String groupSettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Group group = groupService.getGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(group);
        return "group/settings/group";
    }

    @PostMapping("/group/publish")
    public String publishGroup(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        groupService.publish(group);
        attributes.addFlashAttribute("message","모임을 공개했습니다.");
        return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
    }

    @PostMapping("/group/close")
    public String closeGroup(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        groupService.close(group);
        attributes.addFlashAttribute("message","모임을 종료했습니다.");
        return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
    }

    @PostMapping("/group/path")
    public String updateGroupPath(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes,@RequestParam String newPath,Model model) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        if(!groupService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(group);
            model.addAttribute("groupPathError", "해당 모임 경로는 사용할 수 없습니다. 다른 값을 입력하세요");
            return "group/settings/group";
        }
        groupService.updateGroupPath(group,newPath);
        attributes.addFlashAttribute("message","모임 경로를 수정했습니다");
        return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
    }

    @PostMapping("/group/title")
    public String updateGroupTitle(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes,@RequestParam String newTitle,Model model) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        if (!groupService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(group);
            model.addAttribute("groupTitleError","모임 이름을 다시 입력하세요.");
            return "group/settings/group";
        }
        groupService.updateGroupTitle(group,newTitle);
        attributes.addFlashAttribute("message","모임 이름을 수정했습니다.");
        return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
    }


    @PostMapping("/group/remove")
    public String removeStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        groupService.remove(group);
        return "redirect:/";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model, RedirectAttributes attributes) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        if (!group.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
        }

        groupService.startRecruit(group);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다.");
        return "redirect:/group/" + group.getEncodedPath() +"/settings/group";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model, RedirectAttributes attributes) {
        Group group = groupService.getGroupToUpdateStatus(account, path);
        if (!group.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/group/" + group.getEncodedPath() + "/settings/group";
        }

        groupService.stopRecruit(group);
        attributes.addFlashAttribute("message","인원 모집을 종료합니다.");
        return "redirect:/group/" + group.getEncodedPath() +"/settings/group";
    }

}
