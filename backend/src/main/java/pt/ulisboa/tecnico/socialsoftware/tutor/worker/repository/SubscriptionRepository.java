package pt.ulisboa.tecnico.socialsoftware.tutor.worker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.worker.domain.Subscription;

@Repository
@Transactional
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    @Query(value = "SELECT * FROM subscriptions WHERE endpoint = :endpoint", nativeQuery = true)
    Optional<Subscription> findByEndpoint(String endpoint);
}
