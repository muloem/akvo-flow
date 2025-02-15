/*
 *  Copyright (C) 2019 Stichting Akvo (Akvo Foundation)
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

package org.akvo.flow.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gallatinsystems.survey.domain.Translation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gallatinsystems.survey.domain.SurveyGroup;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.gallatinsystems.survey.domain.QuestionGroup;
import com.gallatinsystems.survey.domain.Survey;
import org.waterforpeople.mapping.app.gwt.client.survey.TranslationDto;

@JacksonXmlRootElement(localName = "survey")
public final class XmlForm {

    @JacksonXmlElementWrapper(localName = "altText", useWrapping = false)
    private List<XmlAltText> altText;

    @JacksonXmlElementWrapper(localName = "questionGroup", useWrapping = false)
    private List<XmlQuestionGroup> questionGroup;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "defaultLanguageCode", isAttribute = true)
    private String defaultLanguageCode;

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;

    @JacksonXmlProperty(localName = "app", isAttribute = true)
    private String app;

    @JacksonXmlProperty(localName = "surveyGroupId", isAttribute = true)
    private Long surveyGroupId;

    @JacksonXmlProperty(localName = "surveyGroupName", isAttribute = true)
    private String surveyGroupName;

    @JacksonXmlProperty(localName = "surveyId", isAttribute = true)
    private long surveyId;

    @JacksonXmlProperty(localName = "alias", isAttribute = true)
    private String alias;

    public XmlForm() {
    }

    //Create a form XML object from a form and the name of the containing survey
    public XmlForm(Survey form, SurveyGroup survey, String appStr, String alias) {
        surveyId = form.getKey().getId();
        surveyGroupId = form.getSurveyGroupId();
        surveyGroupName = survey.getCode();
        defaultLanguageCode = form.getDefaultLanguageCode();
        if (defaultLanguageCode == null) {
            defaultLanguageCode = "en";
        }
        name = form.getCode();
        if (name == null){
            name = form.getName();
        }
        version = form.getVersion().toString();
        app = appStr;
        this.alias = alias;
        // Translations, if any
        // Initializing an empty list prevents empty <altText/> tag when there are no translations
        altText = new ArrayList<>();
        if (form.getTranslationMap() != null && !form.getTranslationMap().isEmpty()) {
            for (Translation t: form.getTranslationMap().values()) {
                altText.add(new XmlAltText(t));
            }
        }
        //Now copy the tree of child objects (if any)
        questionGroup = new ArrayList<>(); //Having an empty list prevents a <questionGroup/> tag
        if (form.getQuestionGroupMap() != null) {
            for (QuestionGroup g: form.getQuestionGroupMap().values()) {
                questionGroup.add(new XmlQuestionGroup(g));
            }
        }
    }

    /**
     * @return a Dto object with relevant fields copied
     */
    public SurveyDto toDto() {
        SurveyDto dto = new SurveyDto();
        dto.setKeyId(surveyId);
        dto.setName(name);
        dto.setCode(name);
        dto.setVersion(version);
        //Translations
        if (altText != null) {
            HashMap<String, TranslationDto> qMap = new HashMap<>();
            for (XmlAltText alt : altText) {
                qMap.put(alt.getLanguage(), alt.toDto());
            }
            dto.setTranslationMap(qMap);
        }
        if (questionGroup != null) {
            List<QuestionGroupDto> gList = new ArrayList<>();
            int i = 1;
            for (XmlQuestionGroup g : questionGroup) {
                g.setOrder(i++);
                gList.add(g.toDto());
            }
            dto.setQuestionGroupList(gList);
        }
        //We could add more fields (not needed by the export process) here

        return dto;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<XmlQuestionGroup> getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(List<XmlQuestionGroup> qgl) {
        this.questionGroup = qgl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Long getSurveyGroupId() {
        return surveyGroupId;
    }

    public void setSurveyGroupId(Long surveyGroupId) {
        this.surveyGroupId = surveyGroupId;
    }

    public String getSurveyGroupName() {
        return surveyGroupName;
    }

    public void setSurveyGroupName(String surveyGroupName) {
        this.surveyGroupName = surveyGroupName;
    }

    public long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = Long.parseLong(surveyId);
    }

    public List<XmlAltText> getAltText() {
        return altText;
    }

    public void setAltText(List<XmlAltText> altText) {
        this.altText = altText;
    }

    @Override public String toString() {
        return "Form{" +
                "questionGroups=" + questionGroup.toString() +
                '}';
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
