import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.util.encoders.Base64;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class P7MGenerator {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Load original file
        byte[] originalContent = Files.readAllBytes(Paths.get("original.pdf"));

        // Load public key certificate (X.509 format)
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                Files.newInputStream(Paths.get("public.pem")) // Replace with actual certificate file
        );

        // Get the externally generated signature from API (Base64 encoded)
        String signatureBase64 = "YOUR_SIGNATURE_FROM_API"; // Replace with actual signature
        byte[] signatureBytes = Base64.decode(signatureBase64);

        // Create CMSProcessable data with the original file
        CMSProcessableByteArray cmsData = new CMSProcessableByteArray(originalContent);

        // Generate unsigned CMS structure
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addCertificates(new JcaCertStore(Collections.singletonList(certificate)));
        CMSSignedData unsignedData = generator.generate(cmsData, false);

        // Extract signer information (empty at this stage)
        SignerInformationStore signerStore = unsignedData.getSignerInfos();
        SignerInformation signerInfo = signerStore.getSigners().iterator().next();

        // Manually create a new SignerInformation with the external signature
        SignerInformation newSignerInfo = new SignerInformation(
                signerInfo.toASN1Structure(),
                CMSObjectIdentifiers.data, // Data content type
                signatureBytes // The external signature
        );

        // Replace signers in CMS structure
        CMSSignedData signedData = CMSSignedData.replaceSigners(unsignedData, new SignerInformationStore(newSignerInfo));

        // Save as .p7m file
        try (FileOutputStream fos = new FileOutputStream("signed.p7m")) {
            fos.write(signedData.getEncoded());
        }

        System.out.println("P7M file successfully created.");
    }
}
