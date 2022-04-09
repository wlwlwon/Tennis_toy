package com.tennis.account;

import com.querydsl.core.types.Predicate;
import com.tennis.tag.Tag;
import com.tennis.zone.Zone;
import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        com.tennis.account.QAccount account = com.tennis.account.QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));

    }
}

