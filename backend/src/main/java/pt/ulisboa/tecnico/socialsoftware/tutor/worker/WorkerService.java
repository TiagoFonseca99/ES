// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.domain.Subscription;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto.SubscriptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto.SubscriptionKeyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.repository.SubscriptionRepository;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class WorkerService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private ServerKeys serverKeys;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void subscribe(Integer userId, SubscriptionDto subscriptionDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        checkSubscription(subscriptionDto);

        Subscription subscription = new Subscription(user, subscriptionDto);

        this.entityManager.persist(subscription);

        logger.info("SUCCESSFULLY SUBSCRIBED");
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void unsubscribe(Integer userId, SubscriptionDto subscriptionDto) {
        if (subscriptionDto == null || subscriptionDto.getEndpoint() == null) {
            throw new TutorException(INVALID_SUBSCRIPTION);
        }

        Subscription subscription = subscriptionRepository.findByEndpoint(subscriptionDto.getEndpoint())
                .orElseThrow(() -> new TutorException(SUBSCRIPTION_NOT_FOUND));

        if (subscription.getUser().getId() != userId) {
            throw new TutorException(SUBSCRIPTION_NOT_FROM_USER);
        }


        subscription.remove();

        this.entityManager.remove(subscription);
    }

    public void notifySubscriptions(Notification notification, Collection<User> users) {
        if (notification != null && users != null) {
            List<Subscription> toRemove = new ArrayList<Subscription>();

            logger.info("NOTIFYING {} USERS", users.size());

            try {
                final String message = objectMapper.writeValueAsString(notification);

                for (User user : users) {
                    for (Subscription sub : user.getSubscriptions()) {
                        try {
                            Builder requestBuilder = HttpRequest.newBuilder();
                            byte[] result = cryptoService.encrypt(message, sub.getP256dh(), sub.getAuth(), 0);
                            URL url = new URL(sub.getEndpoint());
                            String origin = url.getProtocol() + "://" + url.getHost();

                            String token = JwtTokenProvider.generateToken(origin, user);

                            URI endpoint = URI.create(sub.getEndpoint());

                            HttpRequest request = requestBuilder.POST(BodyPublishers.ofByteArray(result)).uri(endpoint)
                                    .header("Content-Type", "application/octet-stream")
                                    .header("Content-Encoding", "aes128gcm").header("TTL", "180")
                                    .header("Authorization", "vapid t=" + token + ", k=" + serverKeys.getBase64())
                                    .build();

                            HttpResponse<Void> response = httpClient.send(request, BodyHandlers.discarding());
                            switch (response.statusCode()) {
                                case 201:
                                    break;
                                default:
                                    logger.error("HTTP Response: {} ### {}", response.statusCode(), request);
                            }
                        } catch (InterruptedException | IOException e) {
                            logger.error("Error sending notification");
                            toRemove.add(sub);
                        }
                    }
                }
            } catch (JsonProcessingException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
                    | InvalidAlgorithmParameterException | InvalidKeySpecException | NoSuchAlgorithmException
                    | InvalidKeyException e) {
                logger.error(e.getMessage());
            }
        }
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
