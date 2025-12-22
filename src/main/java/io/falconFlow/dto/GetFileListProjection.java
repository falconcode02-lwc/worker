package io.falconFlow.dto;

import java.time.LocalDateTime;

public interface GetFileListProjection {

  Integer getId();

  String getClassName();

  String getClassType();

  String getFqcn();

  LocalDateTime getCreatedTime();

  LocalDateTime getModifiedTime();

  LocalDateTime getCompiledTime();

  Boolean getIsDeleted();

  Integer getVersion();

  Integer getCounts();
}
