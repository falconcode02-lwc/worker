package io.falconFlow.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

public interface GetWorkFlowsProjection {
     Integer getId();


     String getCode();
     boolean getActive();
     String getName();


     String getController();
     String getWorkflowJson();

     String getDescription();
}
