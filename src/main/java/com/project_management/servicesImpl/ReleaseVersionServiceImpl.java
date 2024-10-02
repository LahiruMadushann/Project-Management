package com.project_management.servicesImpl;

import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.models.Project;
import com.project_management.models.ReleaseVersion;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.services.ReleaseVersionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseVersionServiceImpl implements ReleaseVersionService {

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Override
    public ReleaseVersionDTO createReleaseVersion(ReleaseVersionDTO releaseVersionDTO) {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        BeanUtils.copyProperties(releaseVersionDTO, releaseVersion);
        releaseVersion.setCreatedAt(LocalDateTime.now());
        releaseVersion.setUpdatedAt(LocalDateTime.now());
        ReleaseVersion savedVersion = releaseVersionRepository.save(releaseVersion);
        return convertToDTO(savedVersion);
    }

    @Override
    public ReleaseVersionDTO getReleaseVersionById(Long id) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDTO(releaseVersion);
    }

    @Override
    public List<ReleaseVersionDTO> getAllReleaseVersions() {
        return releaseVersionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReleaseVersionDTO updateReleaseVersion(Long id, ReleaseVersionDTO releaseVersionDTO) {
        ReleaseVersion existingVersion = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        BeanUtils.copyProperties(releaseVersionDTO, existingVersion);
        existingVersion.setUpdatedAt(LocalDateTime.now());
        ReleaseVersion updatedVersion = releaseVersionRepository.save(existingVersion);
        return convertToDTO(updatedVersion);
    }

    @Override
    public void deleteReleaseVersion(Long id) {
        releaseVersionRepository.deleteById(id);
    }

    private ReleaseVersionDTO convertToDTO(ReleaseVersion releaseVersion) {
        ReleaseVersionDTO releaseVersionDTO = new ReleaseVersionDTO();
        BeanUtils.copyProperties(releaseVersion, releaseVersionDTO);
        return releaseVersionDTO;
    }
}
