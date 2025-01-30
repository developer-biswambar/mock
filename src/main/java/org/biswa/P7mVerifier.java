package org.biswa;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Store;

import java.io.File;
import java.io.FileReader;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class P7mVerifier {

    private X509Certificate x509Certificate;

    public  boolean verifyP7mFile(String p7mFilePath) throws Exception {
        // Load the public key from the PEM file

        // Load the .p7m file containing the PKCS#7 signature
        CMSSignedData signedData = loadSignedData(p7mFilePath);

        // Retrieve signer information from the CMSSignedData object
        SignerInformationStore signerStore = signedData.getSignerInfos();

        // Loop through each signer and verify the signature
        for (SignerInformation signerInfo : signerStore.getSigners()) {
            // Verify the signature using the public key
            if (signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(x509Certificate.getPublicKey()))) {
                System.out.println("Signature is valid, the document is not tampered.");
                return true;
            } else {
                System.out.println("Signature verification failed.");
                return false;
            }
        }

        return false; // No signer found
    }


    // Helper function to load signed data from a .p7m file
    private  CMSSignedData loadSignedData(String p7mFilePath) throws Exception {
        File file = new File(p7mFilePath);
        byte[] signedDataBytes = java.nio.file.Files.readAllBytes(file.toPath());

        return new CMSSignedData(signedDataBytes);
    }

}
