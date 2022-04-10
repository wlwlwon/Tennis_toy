package com.tennis.moim;

import com.tennis.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoimRepository extends JpaRepository<Moim,Long> , MoimRepositoryExtension {

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones","managers","members"}, type = EntityGraph.EntityGraphType.LOAD)
    Moim findByPath(String path);

    @EntityGraph(attributePaths = {"tags","managers"})
    Moim findMoimWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones","managers"})
    Moim findMoimWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Moim findMoimWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"zones","tags"})
    Moim findMoimWithTagsAndZonesById(Long id);
    @EntityGraph(attributePaths = "members")
    Moim findMoimWithMembersByPath(String path);

    Moim findMoimOnlyByPath(String path);

    @EntityGraph(attributePaths = {"managers","members"})
    Moim findMoimWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"zones","tags"})
    List<Moim> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Moim> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Moim> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
