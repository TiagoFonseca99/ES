package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;


@Repository
@Transactional
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

}


