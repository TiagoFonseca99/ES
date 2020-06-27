// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto;

public class SubscriptionKeyDto {
    private String p256dh;
    private String auth;

    public SubscriptionKeyDto() {
    }

    public SubscriptionKeyDto(String p256dh, String auth){
        this.p256dh = p256dh;
        this.auth = auth;
    }

    public String getP256dh() {
        return this.p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return this.auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
