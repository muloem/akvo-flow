/*
 *  Copyright (C) 2010-2012,2020 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.waterforpeople.mapping.app.gwt.client.survey;

import com.gallatinsystems.framework.gwt.dto.client.BaseDto;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SurveyDto extends BaseDto {
    private static final long serialVersionUID = 6593732844403807030L;
    private String name;
    private String code;
    private String version;
    private String description;
    private String status;
    private List<QuestionGroupDto> questionGroupList;
    private String path;
    private Long surveyGroupId = null;
    private String defaultLanguageCode;
    private Boolean requireApproval;
    private Date createdDateTime;
    private Date lastUpdateDateTime;
    private Long sourceId = null;
    private List<Long> ancestorIds;
    private String alias;

    private Map<String, TranslationDto> translationMap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(Date lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    public List<QuestionGroupDto> getQuestionGroupList() {
        return questionGroupList;
    }

    public void setQuestionGroupList(List<QuestionGroupDto> questionGroupList) {
        this.questionGroupList = questionGroupList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSurveyGroupId(Long surveyGroupId) {
        this.surveyGroupId = surveyGroupId;
    }

    public Long getSurveyGroupId() {
        return surveyGroupId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setRequireApproval(Boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public Boolean getRequireApproval() {
        return requireApproval;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public List<Long> getAncestorIds() {
        return ancestorIds;
    }

    public void setAncestorIds(List<Long> ancestorIds) {
        this.ancestorIds = ancestorIds;
    }

    public Map<String, TranslationDto> getTranslationMap() {
        return translationMap;
    }

    public void setTranslationMap(Map<String, TranslationDto> translationMap) {
        this.translationMap = translationMap;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
