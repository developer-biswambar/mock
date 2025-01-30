package org.biswa;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class P7mSigner {

    public static void createPKCS7EmbeddedSignature(byte[] originalFile, byte[] signedHash, X509Certificate signerCert, String outputPath) throws Exception {
        // 1. Convert X509Certificate to Bouncy Castle format
        X509CertificateHolder certHolder = new X509CertificateHolder(signerCert.getEncoded());
        Store<X509CertificateHolder> certs = new CollectionStore<>(Collections.singleton(certHolder));

        // 2. Create CMS processable data (original content is embedded)
        CMSProcessableByteArray content = new CMSProcessableByteArray(originalFile);

        // 3. Create a SignerInfoGenerator with pre-signed hash
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        signedAttrs.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new org.bouncycastle.asn1.DEROctetString(signedHash))));

        SignerInfoGenerator signerInfoGen = createSignerInfoGenerator(signedHash,signerCert);

        // 4. Create a SignedData object with the original content and pre-signed hash
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addCertificates(certs);
        generator.addSignerInfoGenerator(signerInfoGen);

        CMSSignedData signedData = generator.generate(content, true); // Embedded signature

        // 5. Write output to .p7m file
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(signedData.getEncoded());
        }
        System.out.println("PKCS#7 Signed File Created: " + outputPath);
    }

    public static void main(String[] args) throws Exception {
        // Load original content
        byte[] originalFile = Files.readAllBytes(Paths.get("/Users/biswambarpradhan/UpSkill/Practice/src/main/java/org/biswa/document.txt"));

        // Load pre-signed hash (Example: SHA-256 hash signed externally)
        byte[] signedHash = Files.readAllBytes(Paths.get("signed-hash.bin"));

        // Load signer's certificate (must match the private key used to sign the hash)
        X509Certificate signerCert = (X509Certificate) java.security.cert.CertificateFactory
                .getInstance("X.509")
                .generateCertificate(Files.newInputStream(Paths.get("signer-cert.pem")));

        // Output path for the .p7m file
        String outputPath = "signed-document.p7m";

        // Create the signed .p7m file
        createPKCS7EmbeddedSignature(originalFile, signedHash, signerCert, outputPath);
    }

    public static SignerInfoGenerator createSignerInfoGenerator(byte[] signedHash, X509Certificate signerCert) throws Exception {
        // Convert Java X509Certificate to Bouncy Castle format
        X509CertificateHolder certHolder = new X509CertificateHolder(signerCert.getEncoded());

        // Create signed attributes including the pre-signed hash
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        signedAttrs.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new org.bouncycastle.asn1.DEROctetString(signedHash))));

        // Create a dummy ContentSigner that injects the pre-signed hash
        ContentSigner dummySigner = new ContentSigner() {
            private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public OutputStream getOutputStream() {
                return outputStream;
            }

            @Override
            public byte[] getSignature() {
                return signedHash; // Inject the externally signed hash
            }

            @Override
            public org.bouncycastle.asn1.x509.AlgorithmIdentifier getAlgorithmIdentifier() {
                return new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
            }
        };

        // Build the SignerInfoGenerator
        return new JcaSignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                .setSignedAttributeGenerator((CMSAttributeTableGenerator) new AttributeTable(signedAttrs))  // Include signed attributes
                .build(dummySigner, certHolder);
    }
}
