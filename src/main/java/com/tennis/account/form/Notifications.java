package com.tennis.account.form;

import lombok.Data;

//@NoArgsConstructor
@Data
public class Notifications {

    private boolean groupCreatedByEmail;

    private boolean groupCreatedByWeb;

    private boolean groupEnrollmentResultByEmail;

    private boolean groupEnrollmentResultByWeb;

    private boolean groupUpdatedByEmail;

    private boolean groupUpdatedByWeb;

}
