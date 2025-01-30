package org.biswa;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class P7mSignerFix {

    public static SignerInfoGenerator createSignerInfoGenerator(byte[] signedHash, X509Certificate signerCert) throws Exception {
        // Convert Java X509Certificate to Bouncy Castle format
        X509CertificateHolder certHolder = new X509CertificateHolder(signerCert.getEncoded());

        // Create signed attributes including the pre-signed hash
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        signedAttrs.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString(signedHash))));

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
                return new org.bouncycastle.asn1.x509.AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);
            }
        };

        // Build the SignerInfoGenerator with signed attributes
        return new JcaSignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(signedAttrs)))
                .build(dummySigner, certHolder);
    }

    public static void createPKCS7EmbeddedSignature(byte[] originalFile, byte[] signedHash, X509Certificate signerCert, String outputPath) throws Exception {
        // Load signer info
        SignerInfoGenerator signerInfoGen = createSignerInfoGenerator(signedHash, signerCert);

        // Convert cert to Bouncy Castle store
        X509CertificateHolder certHolder = new X509CertificateHolder(signerCert.getEncoded());
        Store<X509CertificateHolder> certs = new CollectionStore<>(Collections.singleton(certHolder));

        // Create CMSSignedDataGenerator
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addCertificates(certs);
        generator.addSignerInfoGenerator(signerInfoGen);

        // Generate PKCS#7 with embedded content
        CMSProcessableByteArray content = new CMSProcessableByteArray(originalFile);
        CMSSignedData signedData = generator.generate(content, true); // Embedded signature

        // Write to file
        Files.write(Paths.get(outputPath), signedData.getEncoded());
        System.out.println("PKCS#7 Signed File Created: " + outputPath);
    }
}
