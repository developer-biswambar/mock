import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.cms.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class P7MCreator {
    public static void main(String[] args) throws Exception {
        // Add Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());

        // Load signer's certificate
        X509Certificate signerCert = loadCertificate("signer_cert.pem");

        // Load original content
        byte[] originalContent = loadFile("original_content.txt");

        // Load externally signed hash
        byte[] signedHash = loadFile("externally_signed_hash.bin");

        // Create CMS signed data generator
        CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();

        // Add signer information
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC");
        ContentSigner contentSigner = contentSignerBuilder.build(signerCert.getPublicKey());
        cmsSignedDataGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(contentSigner, signerCert));

        // Create CMS signed data
        CMSSignedData cmsSignedData = cmsSignedDataGenerator.generate(new CMSProcessableByteArray(originalContent), false);

        // Inject externally signed hash
        SignerInformationStore signerInformationStore = cmsSignedData.getSignerInfos();
        SignerInformation signerInformation = signerInformationStore.getSigners().iterator().next();
        signerInformation = new SignerInformation(signerInformation.getSID(), signerInformation.getDigestAlgorithmID(), signedHash);

        // Update signer information store
        signerInformationStore = new SignerInformationStore();
        signerInformationStore.add(signerInformation);

        // Re-encode updated CMS signed data
        cmsSignedData = new CMSSignedData(cmsSignedData.getEncoded(), signerInformationStore);

        // Save CMS signed data to file
        saveCmsSignedData(cmsSignedData, "created_p7m_file.p7m");
    }

    // Helper methods for loading certificate, file, and saving CMS signed data
}