package com.tennis.moim;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.tennis.tag.QTag;
import com.tennis.tag.Tag;
import com.tennis.zone.QZone;
import com.tennis.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class MoimRepositoryExtensionImpl extends QuerydslRepositorySupport implements MoimRepositoryExtension {
    public MoimRepositoryExtensionImpl() {
        super(Moim.class);
    }

    @Override
    public Page<Moim> findByKeyword(String keyword, Pageable pageable) {
        QMoim moim = QMoim.moim;
        JPQLQuery<Moim> query = from(moim).where(moim.published.isTrue()
                        .and(moim.title.containsIgnoreCase(keyword))
                        .or(moim.tags.any().title.containsIgnoreCase(keyword))
                        .or(moim.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(moim.tags, QTag.tag).fetchJoin()
                .leftJoin(moim.zones, QZone.zone).fetchJoin()
                .distinct();
        JPQLQuery<Moim> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Moim> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(),pageable,fetchResults.getTotal());
    }

    @Override
    public List<Moim> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QMoim moim = QMoim.moim;
        JPQLQuery<Moim> query = from(moim).where(moim.published.isTrue()
                        .and(moim.closed.isTrue())
                        .and(moim.tags.any().in(tags))
                        .and(moim.zones.any().in(zones)))
                .leftJoin(moim.tags, QTag.tag).fetchJoin()
                .leftJoin(moim.zones, QZone.zone).fetchJoin()
                .orderBy(moim.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
