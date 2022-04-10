package com.tennis.moim;

import com.tennis.tag.Tag;
import com.tennis.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface MoimRepositoryExtension {

    Page<Moim> findByKeyword(String keyword, Pageable pageable);

    List<Moim> findByAccount(Set<Tag> tags, Set<Zone> zones);
}
