package io.metersphere.service.api;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.RemoteService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ApiPlanService extends RemoteService {

    private static final String BASE_UEL = "/test/plan/api/case";

    public List<String> getExecResultByPlanId(String planId){
        return (List<String>) microService.getForData(MicroServiceName.API_TEST, BASE_UEL + "/plan/exec/result/" + planId);
    }
}
