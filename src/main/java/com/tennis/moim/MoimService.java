package com.tennis.moim;

import com.tennis.account.Account;
import com.tennis.moim.event.MoimCreatedEvent;
import com.tennis.moim.event.MoimUpdateEvent;
import com.tennis.moim.form.MoimDescriptionForm;
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
public class MoimService {

    private final MoimRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    public Moim createNewMoim(Moim moim, Account account) {
        Moim newmoim = repository.save(moim);
        newmoim.addManager(account);
        return newmoim;
    }

    public Moim getMoimToUpdate(Account account, String path) {
        Moim moim = this.getMoim(path);
        checkIfManager(account,moim);
        return moim;
    }

    public Moim getMoim(String path) {
        Moim moim = this.repository.findByPath(path);
        if(moim==null){
            throw new IllegalStateException(path+"에 해당하는 스터디가 없습니다.");
        }
        return moim;
    }

    public void updateMoimDescription(Moim moim, MoimDescriptionForm moimDescriptionForm) {
        modelMapper.map(moimDescriptionForm,moim);
        eventPublisher.publishEvent(new MoimUpdateEvent(moim,"모임을 수정했습니다."));
    }

    public void updateMoimImage(Moim moim, String image) {
        moim.setImage(image);
    }

    public void enableMoimBanner(Moim moim) {
        moim.setUseBanner(true);
    }

    public void disableMoimBanner(Moim moim) {
        moim.setUseBanner(false);
    }

    public void addTag(Moim moim, Tag tag) {
        moim.getTags().add(tag);
    }

    public void removeTag(Moim moim, Tag tag) {
        moim.getTags().remove(tag);
    }

    public void addZone(Moim moim, Zone zone) {
        moim.getZones().add(zone);
    }

    public void removeZone(Moim moim, Zone zone) {
        moim.getZones().remove(zone);
    }

    public Moim getMoimToUpdateTag(Account account, String path) {
        Moim moim = repository.findMoimWithTagsByPath(path);
        checkIfExistingMoim(path,moim);
        checkIfManager(account,moim);
        return moim;
    }

    private void checkIfManager(Account account, Moim moim) {
        if (!moim.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingMoim(String path, Moim moim) {
        if(moim==null){
            throw new IllegalArgumentException(path+"에 해당하는 스터디가 없습니다.");
        }
    }

    public Moim getMoimToUpdateZone(Account account, String path) {
        Moim moim = repository.findMoimWithZonesByPath(path);
        checkIfExistingMoim(path,moim);
        checkIfManager(account,moim);
        return moim;
    }

    public Moim getMoimToUpdateStatus(Account account, String path) {
        Moim moim = repository.findMoimWithManagersByPath(path);
        checkIfExistingMoim(path,moim);
        checkIfManager(account,moim);
        return moim;
    }

    public void publish(Moim moim) {
        moim.publish();
        eventPublisher.publishEvent(new MoimCreatedEvent(moim));

    }

    public void close(Moim moim) {
        moim.closed();
        eventPublisher.publishEvent(new MoimUpdateEvent(moim,"스터디를 종료했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches("^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$")) {
            return false;
        }
        return !repository.existsByPath(newPath);
    }

    public void updateMoimPath(Moim moim, String newPath) {
        moim.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length()<=50;
    }

    public void updateMoimTitle(Moim moim, String newTitle) {
        moim.setTitle(newTitle);
    }

    public void remove(Moim moim) {
        if (moim.isRemovable()) {
            repository.delete(moim);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Moim moim, Account account) {
        moim.addMember(account);
    }

    public void removeMember(Moim moim, Account account) {
        moim.removeMember(account);
    }

    public void startRecruit(Moim moim) {
        moim.startRecruit();
        eventPublisher.publishEvent(new MoimUpdateEvent(moim,"팀원 모집을 시작합니다."));

    }

    public void stopRecruit(Moim moim) {
        moim.stopRecruit();
        eventPublisher.publishEvent(new MoimUpdateEvent(moim,"팀원 모집을 중단했습니다.."));
    }

    public Moim getMoimToEnroll(String path) {
        Moim moim = repository.findMoimOnlyByPath(path);
        checkIfExistingMoim(path,moim);
        return moim;

    }

    public void generateTestMoim(Account account) {
        for (int i = 0; i < 30; i++) {
            String randomvalue = RandomString.make(5);
            Moim moim = Moim.builder()
                    .title("테스트 스터디 " + randomvalue)
                    .path("test-" + randomvalue)
                    .shortDescription("test moim")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            moim.publish();
            Moim newMoim = this.createNewMoim(moim, account);
            Tag jpa = tagRepository.findByTitle("JPA");
            newMoim.getTags().add(jpa);
        }
    }
}
