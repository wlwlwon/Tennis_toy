package com.tennis.group;

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

public class GroupRepositoryExtensionImpl extends QuerydslRepositorySupport implements GroupRepositoryExtension{
    public GroupRepositoryExtensionImpl() {
        super(Group.class);
    }

    @Override
    public Page<Group> findByKeyword(String keyword, Pageable pageable) {
        QGroup group = QGroup.group;
        JPQLQuery<Group> query = from(group).where(group.published.isTrue()
                        .and(group.title.containsIgnoreCase(keyword))
                        .or(group.tags.any().title.containsIgnoreCase(keyword))
                        .or(group.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(group.tags, QTag.tag).fetchJoin()
                .leftJoin(group.zones, QZone.zone).fetchJoin()
                .distinct();
        JPQLQuery<Group> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Group> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(),pageable,fetchResults.getTotal());
    }

    @Override
    public List<Group> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QGroup group = QGroup.group;
        JPQLQuery<Group> query = from(group).where(group.published.isTrue()
                        .and(group.closed.isTrue())
                        .and(group.tags.any().in(tags))
                        .and(group.zones.any().in(zones)))
                .leftJoin(group.tags, QTag.tag).fetchJoin()
                .leftJoin(group.zones, QZone.zone).fetchJoin()
                .orderBy(group.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
