package com.medvault.mapper;

import com.medvault.dto.MedicalRecordResponse;
import com.medvault.entity.MedicalRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponse toResponse(MedicalRecord record) {
        if (record == null) {
            return null;
        }

        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setPatientId(record.getPatientId());
        response.setUploadedBy(record.getUploadedBy());
        response.setFileName(record.getFileName());
        response.setFileType(record.getFileType());
        response.setCategory(record.getCategory());
        response.setFileUrl(record.getFileUrl());
        response.setNotes(record.getNotes());
        response.setVersionNumber(record.getVersionNumber());
        response.setIsActive(record.getIsActive());
        response.setUploadDate(record.getUploadDate());
        response.setLastModifiedDate(record.getLastModifiedDate());
        return response;
    }

    public List<MedicalRecordResponse> toResponseList(List<MedicalRecord> records) {
        if (records == null) {
            return null;
        }
        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
