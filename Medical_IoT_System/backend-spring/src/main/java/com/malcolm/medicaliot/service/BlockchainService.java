package com.malcolm.medicaliot.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BlockchainService {

    private final List<Block> ledger = new ArrayList<>();

    public String logTransaction(String patientId, String ipfsCid, String description) {
        String prevHash = ledger.isEmpty() ? "0" : ledger.get(ledger.size() - 1).getHash();
        Block newBlock = new Block(prevHash, patientId, ipfsCid, description);
        ledger.add(newBlock);

        System.out.println("--- [BLOCKCHAIN] New Block Mined ---");
        System.out.println("Hash: " + newBlock.getHash());
        System.out.println("Data: " + newBlock.getData());
        System.out.println("------------------------------------");

        return newBlock.getHash();
    }

    // Inner class for Block structure
    private static class Block {
        private String prevHash;
        private String hash;
        private String data;
        private LocalDateTime timestamp;

        public Block(String prevHash, String patientId, String ipfsCid, String description) {
            this.prevHash = prevHash;
            this.timestamp = LocalDateTime.now();
            this.data = "Patient:" + patientId + "|IPFS:" + ipfsCid + "|Action:" + description;
            this.hash = calculateHash();
        }

        public String getHash() {
            return hash;
        }

        @Override
        public String toString() {
            return "Block{prevHash='" + prevHash + "', timestamp=" + timestamp + ", hash='" + hash + "'}";
        }

        public String getData() {
            return data;
        }

        private String calculateHash() {
            // Simple Mock Hash
            return UUID.randomUUID().toString();
        }
    }
}
