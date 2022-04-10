package com.tennis.moim;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import com.tennis.moim.form.MoimDescriptionForm;
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
@RequestMapping("/moim/{path}/settings")
@RequiredArgsConstructor
public class MoimSettingsController {

    private final MoimService moimService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;


    @GetMapping("/tags")
    public String moimTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(moim);


        model.addAttribute("tags", moim.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTagTitles));

        return "moim/settings/tags";
    }

    @PostMapping( "/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm, @PathVariable String path){
        Moim moim = moimService.getMoimToUpdateTag(account, path);
        //Moim moim = moimService.getMoimToUpdate(account,path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        moimService.addTag(moim,tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm,@PathVariable String path){
        Moim moim = moimService.getMoimToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        moimService.removeTag(moim,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String moimZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(moim);

        model.addAttribute("zones",moim.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));
        return "moim/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm, @PathVariable String path){
        Moim moim = moimService.getMoimToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){
            return ResponseEntity.badRequest().build();
        }
        moimService.addZone(moim,zone);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm,@PathVariable String path){
        Moim moim = moimService.getMoimToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){
            return ResponseEntity.badRequest().build();
        }
        moimService.removeZone(moim,zone);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/banner")
    public String moimImageForm(@CurrentAccount Account account, @PathVariable String path, Model model){
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(moim);
        return "moim/settings/banner";
    }
    @PostMapping("/banner")
    public String moimImageSubmit(@CurrentAccount Account account, @PathVariable String path,
                                   Model model, Errors errors, RedirectAttributes attributes, String image) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        moimService.updateMoimImage(moim,image);
        attributes.addFlashAttribute("message", "모임 이미지를 수정했습니다.");
        return "redirect:/moim/" + moim.getEncodedPath() +"/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableMoimBanner(@CurrentAccount Account account, @PathVariable String path) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        moimService.enableMoimBanner(moim);
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/banner";
    }
    @PostMapping("/banner/disable")
    public String disableMoimBanner(@CurrentAccount Account account, @PathVariable String path) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        moimService.disableMoimBanner(moim);
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/banner";
    }

    @GetMapping("/description")
    public String viewMoimSetting(@CurrentAccount Account account,@PathVariable String path, Model model) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(moim);
        model.addAttribute(modelMapper.map(moim, MoimDescriptionForm.class));
        return "moim/settings/description";

    }

    @PostMapping("/description")
    public String updateMoimInfo(@CurrentAccount Account account, @PathVariable String path, @Valid MoimDescriptionForm moimDescriptionForm,
                                  Errors errors, Model model, RedirectAttributes redirectAttributes) {
        Moim moim = moimService.getMoimToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(moim);
            return "moim/settings/description";
        }
        moimService.updateMoimDescription(moim,moimDescriptionForm);
        redirectAttributes.addFlashAttribute("message", "모임 소개를 수정했습니다.");
        return "redirect:/moim/" + moim.getEncodedPath() +"/settings/description";
    }

    @GetMapping("/moim")
    public String moimSettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(moim);
        return "moim/settings/moim";
    }

    @PostMapping("/moim/publish")
    public String publishMoim(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        moimService.publish(moim);
        attributes.addFlashAttribute("message","모임을 공개했습니다.");
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
    }

    @PostMapping("/moim/close")
    public String closeMoim(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        moimService.close(moim);
        attributes.addFlashAttribute("message","모임을 종료했습니다.");
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
    }

    @PostMapping("/moim/path")
    public String updateMoimPath(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes,@RequestParam String newPath,Model model) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        if(!moimService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(moim);
            model.addAttribute("moimPathError", "해당 모임 경로는 사용할 수 없습니다. 다른 값을 입력하세요");
            return "moim/settings/moim";
        }
        moimService.updateMoimPath(moim,newPath);
        attributes.addFlashAttribute("message","모임 경로를 수정했습니다");
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
    }

    @PostMapping("/moim/title")
    public String updateMoimTitle(@CurrentAccount Account account, @PathVariable String path,RedirectAttributes attributes,@RequestParam String newTitle,Model model) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        if (!moimService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(moim);
            model.addAttribute("moimTitleError","모임 이름을 다시 입력하세요.");
            return "moim/settings/moim";
        }
        moimService.updateMoimTitle(moim,newTitle);
        attributes.addFlashAttribute("message","모임 이름을 수정했습니다.");
        return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
    }


    @PostMapping("/moim/remove")
    public String removeMoim(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        moimService.remove(moim);
        return "redirect:/";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model, RedirectAttributes attributes) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        if (!moim.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
        }

        moimService.startRecruit(moim);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다.");
        return "redirect:/moim/" + moim.getEncodedPath() +"/settings/moim";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model, RedirectAttributes attributes) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        if (!moim.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/moim/" + moim.getEncodedPath() + "/settings/moim";
        }

        moimService.stopRecruit(moim);
        attributes.addFlashAttribute("message","인원 모집을 종료합니다.");
        return "redirect:/moim/" + moim.getEncodedPath() +"/settings/moim";
    }

}
