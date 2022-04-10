package com.tennis.account.form;

import lombok.Data;

//@NoArgsConstructor
@Data
public class Notifications {

    private boolean moimCreatedByEmail;

    private boolean moimCreatedByWeb;

    private boolean moimEnrollmentResultByEmail;

    private boolean moimEnrollmentResultByWeb;

    private boolean moimUpdatedByEmail;

    private boolean moimUpdatedByWeb;

}
