import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
public class RsaCrossCompat {
  public static void main(String[] a) throws Exception {
    // deterministic keypair
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();
    String pkcs8 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    String spki  = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    String text = "byte-compat-test-12345";
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(kp.getPrivate());
    sig.update(text.getBytes("UTF-8"));
    String javaSig = Base64.getEncoder().encodeToString(sig.sign());
    // print as JSON for node to consume
    System.out.println("PKCS8=" + pkcs8);
    System.out.println("SPKI=" + spki);
    System.out.println("JAVA_SIG=" + javaSig);
  }
}
