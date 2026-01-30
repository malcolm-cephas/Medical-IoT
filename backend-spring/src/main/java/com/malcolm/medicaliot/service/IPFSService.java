package com.malcolm.medicaliot.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class IPFSService {

    public String store(String encryptedData) {
        // Mock IPFS Logic
        // In reality, this would upload to an IPFS node and return a Multihash CID
        System.out.println("--- [IPFS] Storing Data ---");
        System.out.println("Payload Size: " + encryptedData.length());

        String cid = "Qm" + UUID.randomUUID().toString().replace("-", "") + "z";

        // Simulate storage delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        System.out.println("--- [IPFS] Stored Successfully. CID: " + cid + " ---");
        return cid;
    }

    public String retrieve(String cid) {
        // Mock Retrieval
        return "EncryptedDataFromIPFS_Mock";
    }
}
