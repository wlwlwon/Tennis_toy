package com.tennis.moim.validator;

import com.tennis.moim.MoimRepository;
import com.tennis.moim.form.MoimForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MoimFormValidator implements Validator {

    private final MoimRepository moimRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return MoimForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MoimForm moimForm = (MoimForm) o;
        if(moimRepository.existsByPath(moimForm.getPath())){
            errors.rejectValue("path","wrong.path","해당 모임 경로값을 사용할 수 없습니다.");
        }
    }
}

