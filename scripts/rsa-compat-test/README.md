# RSA byte-compatibility test (PR4)

Proves the **native Android crypto path produces byte-identical signatures** to the
existing web `crypto.subtle` scheme — the single highest-risk compatibility point
in the SunMint mobile port (plan §1.3 / §2).

## The scheme (must match web app exactly)
- Algorithm: `RSASSA-PKCS1-v1_5` (PKCS#1 v1.5), 2048-bit modulus, SHA-256
- Export format: public key **SPKI**, private key **PKCS8**, base64-encoded
- Signing input: the raw UTF-8 bytes of the text payload (`encoder.encode(text)` in the web app)
- Android native equivalent: `java.security.KeyPairGenerator` ("RSA", 2048) +
  `Signature.getInstance("SHA256withRSA")`

## Why byte-identical matters
RSASSA-PKCS1-v1_5 is **deterministic**: for the same key + same input bytes, every
implementation produces the same 256-byte signature. Edgar/dao_protocol verify
signatures against the SPKI/PKCS8 keys — so if native Android signing is even one
byte off, every submission fails. This test proves it isn't.

## Run
```bash
cd scripts/rsa-compat-test
javac RsaCrossCompat.java && java RsaCrossCompat > /tmp/rsa_out.txt
node cross-check.js   # imports same PKCS8, signs same payload, compares
# expect: RESULT: BYTE-IDENTICAL
```

## Files
- `RsaCompatTest.java` — native generate/sign/verify self-test (SPKI 294B, PKCS8 1216B, sig 256B)
- `RsaCrossCompat.java` — emits a keypair + Java signature for cross-check
- `cross-check.js` — Node side: imports the same PKCS8, signs the same payload,
  asserts byte-identity with the Java signature
