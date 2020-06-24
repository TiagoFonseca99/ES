// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServerKeys {
    @Autowired
    private CryptoService cryptoService;

    @Value("${public.key.path}")
    private String pubPath;

    @Value("${private.key.path}")
    private String privPath;

    private ECPublicKey pubKey;

    private ECPrivateKey privKey;

    private byte[] pubKeyUncompressed;

    private String pubKeyB64;

    private final static Logger logger = LoggerFactory.getLogger(ServerKeys.class);

    public byte[] getUncompressed() {
        return this.pubKeyUncompressed;
    }

    @PostConstruct
    private void initKeys() {
        Path pubFile = Paths.get(this.pubPath);
        Path privFile = Paths.get(this.privPath);

        if (Files.exists(pubFile) && Files.exists(privFile)) {
            try {
                byte[] pubKey = Files.readAllBytes(pubFile);
                byte[] privKey = Files.readAllBytes(privFile);

                this.pubKey = (ECPublicKey) this.cryptoService.convertX509ToECPublicKey(pubKey);
                this.privKey = (ECPrivateKey) this.cryptoService.convertPKCS8ToECPrivateKey(privKey);

                this.pubKeyUncompressed = CryptoService.toUncompressedECPublicKey(this.pubKey);

                this.pubKeyB64 = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(this.pubKeyUncompressed);
            } catch (IOException | InvalidKeySpecException e) {
                logger.error("Couldn't read key files", e);
            }
        } else {
            try {
                KeyPair pair = this.cryptoService.getKeyPairGenerator().generateKeyPair();
                this.pubKey = (ECPublicKey) pair.getPublic();
                this.privKey = (ECPrivateKey) pair.getPrivate();
                Files.write(pubFile, this.pubKey.getEncoded());
                Files.write(privFile, this.privKey.getEncoded());

                this.pubKeyUncompressed = CryptoService.toUncompressedECPublicKey(this.pubKey);

                this.pubKeyB64 = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(this.pubKeyUncompressed);
            } catch (IOException e) {
                logger.error("Couldn't write key files", e);
            }
        }

    }

}
