package com.tennis.group;

import com.tennis.account.Account;
import com.tennis.group.event.GroupCreatedEvent;
import com.tennis.group.event.GroupUpdateEvent;
import com.tennis.group.form.GroupDescriptionForm;
import com.tennis.tag.Tag;
import com.tennis.tag.TagRepository;
import com.tennis.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    public Group createNewGroup(Group group, Account account) {
        Group newgroup = repository.save(group);
        newgroup.addManager(account);
        return newgroup;
    }

    public Group getGroupToUpdate(Account account, String path) {
        Group group = this.getGroup(path);
        checkIfManager(account,group);
        return group;
    }

    public Group getGroup(String path) {
        Group group = this.repository.findByPath(path);
        if(group==null){
            throw new IllegalStateException(path+"에 해당하는 스터디가 없습니다.");
        }
        return group;
    }

    public void updateGroupDescription(Group group, GroupDescriptionForm groupDescriptionForm) {
        modelMapper.map(groupDescriptionForm,group);
        eventPublisher.publishEvent(new GroupUpdateEvent(group,"모임을 수정했습니다."));
    }

    public void updateGroupImage(Group group, String image) {
        group.setImage(image);
    }

    public void enableGroupBanner(Group group) {
        group.setUseBanner(true);
    }

    public void disableGroupBanner(Group group) {
        group.setUseBanner(false);
    }

    public void addTag(Group group, Tag tag) {
        group.getTags().add(tag);
    }

    public void removeTag(Group group, Tag tag) {
        group.getTags().remove(tag);
    }

    public void addZone(Group group, Zone zone) {
        group.getZones().add(zone);
    }

    public void removeZone(Group group, Zone zone) {
        group.getZones().remove(zone);
    }

    public Group getGroupToUpdateTag(Account account, String path) {
        Group group = repository.findGroupWithTagsByPath(path);
        checkIfExistingGroup(path,group);
        checkIfManager(account,group);
        return group;
    }

    private void checkIfManager(Account account, Group group) {
        if (!group.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingGroup(String path, Group group) {
        if(group==null){
            throw new IllegalArgumentException(path+"에 해당하는 스터디가 없습니다.");
        }
    }

    public Group getGroupToUpdateZone(Account account, String path) {
        Group group = repository.findGroupWithZonesByPath(path);
        checkIfExistingGroup(path,group);
        checkIfManager(account,group);
        return group;
    }

    public Group getGroupToUpdateStatus(Account account, String path) {
        Group group = repository.findGroupWithManagersByPath(path);
        checkIfExistingGroup(path,group);
        checkIfManager(account,group);
        return group;
    }

    public void publish(Group group) {
        group.publish();
        eventPublisher.publishEvent(new GroupCreatedEvent(group));

    }

    public void close(Group group) {
        group.closed();
        eventPublisher.publishEvent(new GroupUpdateEvent(group,"스터디를 종료했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches("^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$")) {
            return false;
        }
        return !repository.existsByPath(newPath);
    }

    public void updateGroupPath(Group group, String newPath) {
        group.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length()<=50;
    }

    public void updateGroupTitle(Group group, String newTitle) {
        group.setTitle(newTitle);
    }

    public void remove(Group group) {
        if (group.isRemovable()) {
            repository.delete(group);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Group group, Account account) {
        group.addMember(account);
    }

    public void removeMember(Group group, Account account) {
        group.removeMember(account);
    }

    public void startRecruit(Group group) {
        group.startRecruit();
        eventPublisher.publishEvent(new GroupUpdateEvent(group,"팀원 모집을 시작합니다."));

    }

    public void stopRecruit(Group group) {
        group.stopRecruit();
        eventPublisher.publishEvent(new GroupUpdateEvent(group,"팀원 모집을 중단했습니다.."));
    }

    public Group getGroupToEnroll(String path) {
        Group group = repository.findGroupOnlyByPath(path);
        checkIfExistingGroup(path,group);
        return group;

    }

    public void generateTestStudies(Account account) {
        for (int i = 0; i < 30; i++) {
            String randomvalue = RandomString.make(5);
            Group group = Group.builder()
                    .title("테스트 스터디 " + randomvalue)
                    .path("test-" + randomvalue)
                    .shortDescription("test group")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            group.publish();
            Group newGroup = this.createNewGroup(group, account);
            Tag jpa = tagRepository.findByTitle("JPA");
            newGroup.getTags().add(jpa);
        }
    }
}
