package com.malcolm.medicaliot.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BlockchainService {

    private final List<Block> ledger = new ArrayList<>();

    public BlockchainService() {
        // Genesis Block
        ledger.add(new Block("0", "SYSTEM", "GENESIS_BLOCK", "System Initialized"));
    }

    public synchronized String logTransaction(String patientId, String ipfsCid, String description) {
        String prevHash = ledger.isEmpty() ? "0" : ledger.get(ledger.size() - 1).getHash();
        Block newBlock = new Block(prevHash, patientId, ipfsCid, description);
        ledger.add(newBlock);

        System.out.println("--- [BLOCKCHAIN] New Block Mined ---");
        System.out.println("Hash: " + newBlock.getHash());
        System.out.println("Prev: " + newBlock.getPrevHash());
        System.out.println("Data: " + newBlock.getData());
        System.out.println("------------------------------------");

        return newBlock.getHash();
    }

    public List<Block> getChain() {
        return Collections.unmodifiableList(ledger);
    }

    // Inner class for Block structure
    public static class Block {
        private String prevHash;
        private String hash;
        private String data;
        private LocalDateTime timestamp;
        private long nonce;

        public Block(String prevHash, String patientId, String ipfsCid, String description) {
            this.prevHash = prevHash;
            this.timestamp = LocalDateTime.now();
            this.data = "Patient:" + patientId + "|IPFS:" + ipfsCid + "|Action:" + description;
            this.hash = calculateHash();
        }

        public String getHash() {
            return hash;
        }

        public String getPrevHash() {
            return prevHash;
        }

        public String getData() {
            return data;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        private String calculateHash() {
            String input = prevHash + timestamp.toString() + data + nonce;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
