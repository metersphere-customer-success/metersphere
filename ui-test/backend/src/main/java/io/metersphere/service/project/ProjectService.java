package io.metersphere.service.project;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService extends RemoteService {

    public List<ProjectDTO> getOwnerProjects(){
        return (List<ProjectDTO>) microService.getForData(MicroServiceName.PROJECT_MANAGEMENT,  "/project/get-owner-projects");
    }

    public ProjectService() {
        super(MicroServiceName.PROJECT_MANAGEMENT);
    }
}
