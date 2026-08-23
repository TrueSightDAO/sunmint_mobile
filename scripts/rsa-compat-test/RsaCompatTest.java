import java.security.*;
import java.util.Base64;
public class RsaCompatTest {
  public static void main(String[] a) throws Exception {
    // 1. Generate keypair via native Android-compatible API
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();
    // 2. Export SPKI/PKCS8 base64 (same as crypto.subtle exportKey)
    String spki = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    String pkcs8 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    System.out.println("SPKI_len=" + kp.getPublic().getEncoded().length + " PKCS8_len=" + kp.getPrivate().getEncoded().length);
    // 3. Sign payload with SHA256withRSA (RSASSA-PKCS1-v1_5 + SHA-256)
    String text = "test-signature-payload-12345";
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(kp.getPrivate());
    sig.update(text.getBytes("UTF-8"));
    byte[] signature = sig.sign();
    System.out.println("SIG_len=" + signature.length);
    System.out.println("SIG_b64=" + Base64.getEncoder().encodeToString(signature));
    // 4. Verify
    Signature ver = Signature.getInstance("SHA256withRSA");
    ver.initVerify(kp.getPublic());
    ver.update(text.getBytes("UTF-8"));
    System.out.println("VERIFY=" + ver.verify(signature));
  }
}
