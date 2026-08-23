// Node cross-check: import the SAME PKCS8 key Java signed with, sign the SAME
// payload, and assert the signatures are byte-identical (RSASSA-PKCS1-v1_5 is
// deterministic, so equality proves cross-implementation byte-compatibility).
const crypto = require('crypto');
const fs = require('fs');
const lines = fs.readFileSync(process.argv[2] || '/tmp/rsa_out.txt', 'utf8').split('\n').filter(Boolean);
const pkcs8   = lines.find(l => l.startsWith('PKCS8=')).slice(6);
const spki    = lines.find(l => l.startsWith('SPKI=')).slice(5);
const javaSig = lines.find(l => l.startsWith('JAVA_SIG=')).slice(9);
const text    = 'byte-compat-test-12345';
const priv = crypto.createPrivateKey({ key: Buffer.from(pkcs8, 'base64'), format: 'der', type: 'pkcs8' });
const pub  = crypto.createPublicKey({ key: Buffer.from(spki, 'base64'), format: 'der', type: 'spki' });
const nodeSig = crypto.sign('sha256', Buffer.from(text, 'utf8'), priv).toString('base64');
// also verify Java's signature with the public key (proves Edgar-side verify works)
const verifies = crypto.verify('sha256', Buffer.from(text, 'utf8'), pub, Buffer.from(javaSig, 'base64'));
console.log('java_sig == node_sig :', javaSig === nodeSig);
console.log('java_sig verifies    :', verifies);
console.log(javaSig === nodeSig && verifies ? 'RESULT: BYTE-IDENTICAL' : 'RESULT: MISMATCH');
process.exit(javaSig === nodeSig && verifies ? 0 : 1);
