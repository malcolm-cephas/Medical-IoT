package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.MedicalRecord;
import com.malcolm.medicaliot.repository.MedicalRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MedicalRecordService {

    @Autowired
    private IPFSService ipfsService;

    @Autowired
    private MedicalRecordRepository repository;

    /**
     * Complete workflow: Upload to IPFS, get CID, then persist metadata in MySQL.
     * We only store the ipfsHash in the database to keep medical data sovereign.
     */
    public MedicalRecord processAndStoreRecord(String patientId, String description, MultipartFile file) {
        log.info("Processing clinical record for patient: {}", patientId);

        // 1. Upload the raw medical file to IPFS decentralised storage
        String cid = ipfsService.uploadToIpfs(file);

        // 2. Prevent redundant records if the CID already exists (content addressable)
        if (repository.existsByIpfsHash(cid)) {
             log.warn("CID {} already exists in registry. Re-using existing medical record.", cid);
        }

        // 3. Create the medical record shell for database mapping
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDescription(description);
        record.setFileName(file.getOriginalFilename());
        record.setFileType(file.getContentType());
        record.setIpfsHash(cid); // Content-based addressing
        record.setUploadedAt(LocalDateTime.now());

        // 4. Finally, persist the shell metadata
        return repository.save(record);
    }

    public List<MedicalRecord> getPatientRecords(String patientId) {
        return repository.findByPatientId(patientId);
    }
}
