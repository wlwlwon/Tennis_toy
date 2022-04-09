package com.tennis.group.validator;

import com.tennis.group.GroupRepository;
import com.tennis.group.form.GroupForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class GroupFormValidator implements Validator {

    private final GroupRepository groupRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return GroupForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        GroupForm groupForm = (GroupForm) o;
        if(groupRepository.existsByPath(groupForm.getPath())){
            errors.rejectValue("path","wrong.path","해당 모임 경로값을 사용할 수 없습니다.");
        }
    }
}

