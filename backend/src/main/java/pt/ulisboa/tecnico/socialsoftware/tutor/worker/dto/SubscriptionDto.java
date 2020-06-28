// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto;

public class SubscriptionDto {
    private String endpoint;
    private Long expirationTime;
    private SubscriptionKeyDto keys;

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Long getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(Long expiration) {
        this.expirationTime = expiration;
    }

    public SubscriptionKeyDto getKeys() {
        return this.keys;
    }

    public void setKeys(SubscriptionKeyDto keys) {
        this.keys = keys;
    }
}
