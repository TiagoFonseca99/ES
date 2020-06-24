// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker;

import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.domain.Subscription;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto.SubscriptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto.SubscriptionKeyDto;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class WorkerService {
    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void subscribe(Integer userId, SubscriptionDto subscriptionDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        checkSubscription(subscriptionDto);

        Subscription subscription = new Subscription(user, subscriptionDto);

        this.entityManager.persist(subscription);
    }

    private void checkSubscription(SubscriptionDto subscription) {
        if (subscription == null || subscription.getExpirationTime() == null
                || subscription.getExpirationTime() < new Date().getTime() || subscription.getEndpoint() == null
                || !validSubscriptionKey(subscription.getKeys())) {
            throw new TutorException(INVALID_SUBSCRIPTION);
        }
    }

    private boolean validSubscriptionKey(SubscriptionKeyDto key) {
        return key != null && key.getAuth() != null && key.getP256dh() != null;
    }
}
