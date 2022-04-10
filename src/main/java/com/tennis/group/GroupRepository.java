package com.tennis.group;

import com.tennis.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long> ,GroupRepositoryExtension{

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones","managers","members"}, type = EntityGraph.EntityGraphType.LOAD)
    Group findByPath(String path);

    @EntityGraph(attributePaths = {"tags","managers"})
    Group findGroupWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones","managers"})
    Group findGroupWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Group findGroupWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"zones","tags"})
    Group findGroupWithTagsAndZonesById(Long id);
    @EntityGraph(attributePaths = "members")
    Group findGroupWithMembersByPath(String path);

    Group findGroupOnlyByPath(String path);

    @EntityGraph(attributePaths = {"managers","members"})
    Group findGroupWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"zones","tags"})
    List<Group> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Group> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Group> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
