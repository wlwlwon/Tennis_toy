package com.tennis.event;


import com.tennis.account.Account;
import com.tennis.account.CurrentAccount;
import com.tennis.event.form.EventForm;
import com.tennis.event.validator.EventValidator;
import com.tennis.moim.Moim;
import com.tennis.moim.MoimService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/moim/{path}")
@RequiredArgsConstructor
public class EventController {

    private final MoimService moimService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        model.addAttribute(moim);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";

    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path, @Valid EventForm eventForm, Errors errors, Model model) {

        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(moim);
            return "event/form";
        }
        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), moim, account);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(moimService.getMoim(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewMoimEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Moim moim = moimService.getMoim(path);
        model.addAttribute(account);
        model.addAttribute(moim);

        List<Event> events = eventRepository.findByMoimOrderByStartDateTime(moim);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "moim/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        model.addAttribute(moim);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id")Event event, @Valid EventForm eventForm, Errors errors, Model model) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(moim);
            model.addAttribute(event);
            return "event/update-form";
        }
        eventService.updateEvent(event, eventForm);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Moim moim = moimService.getMoimToUpdateStatus(account, path);
        eventService.deleteEvent(event);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event ){
        Moim moim = moimService.getMoimToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Moim moim = moimService.getMoimToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId")Enrollment enrollment) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        eventService.acceptEnrollment(event,enrollment);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId")Enrollment enrollment) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        eventService.rejectEnrollment(event,enrollment);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Moim moim = moimService.getMoimToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/moim/" + moim.getEncodedPath() + "/events/" + event.getId();
    }

}
