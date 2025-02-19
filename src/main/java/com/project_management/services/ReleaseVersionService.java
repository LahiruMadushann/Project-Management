package com.project_management.services;

import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.dto.ReleaseVersionDTONew;

import java.util.List;

public interface ReleaseVersionService {
    ReleaseVersionDTO createReleaseVersion(ReleaseVersionDTO releaseVersionDTO);
    ReleaseVersionDTO getReleaseVersionById(Long id);
    List<ReleaseVersionDTO> getAllReleaseVersions();
    ReleaseVersionDTO updateReleaseVersion(Long id, ReleaseVersionDTO releaseVersionDTO);
    void deleteReleaseVersion(Long id);
    List<ReleaseVersionDTO> getReleaseVersionsByProjectId(Long projectId);
    List<ReleaseVersionDTONew> getReleaseVersionsByProjectIdNew(Long projectId);
}
