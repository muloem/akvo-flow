package org.akvo.flow.xml;

import java.util.ArrayList;
import java.util.Arrays;

import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/*
 * Class for working with a form XML file like this:
 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 * <survey name="Brand new form four" defaultLanguageCode="en" version='4.0' app="akvoflowsandbox"
 *  surveyGroupId="41213002" surveyGroupName="Brand new" surveyId="43993002">
 * <questionGroup><heading>Foo</heading>
 * ...
 * </questionGroup>
 * <questionGroup><heading>Bar</heading>
 * ...
 * </questionGroup>
 * </survey>
 */

@JacksonXmlRootElement(localName = "survey") public final class XmlForm {

    @JacksonXmlElementWrapper(localName = "questionGroup", useWrapping = false)
    private XmlQuestionGroup[] questionGroup;

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "defaultLanguageCode", isAttribute = true)
    private String defaultLanguageCode;

    @JacksonXmlProperty(localName = "app", isAttribute = true)
    private String app;

    @JacksonXmlProperty(localName = "surveyGroupId", isAttribute = true)
    private String surveyGroupId;

    @JacksonXmlProperty(localName = "surveyGroupName", isAttribute = true)
    private String surveyGroupName;

    @JacksonXmlProperty(localName = "surveyId", isAttribute = true)
    private long surveyId;

    public XmlForm() {
    }

    /**
     * @return a Dto object with relevant fields copied
     */
    public SurveyDto toDto() {
        SurveyDto dto = new SurveyDto();
        //TODO: lots
        dto.setKeyId(surveyId);
        dto.setName(name);
        dto.setCode(name);
        ArrayList<QuestionGroupDto> gList = new ArrayList<>();
        int i = 1;
        for (XmlQuestionGroup g : questionGroup) {
            g.setOrder(i++);
            gList.add(g.toDto());
        }
        dto.setQuestionGroupList(gList);

        return dto;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public XmlForm(XmlQuestionGroup[] questionGroups) {
        this.questionGroup = questionGroups;
    }

    public XmlQuestionGroup[] getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(XmlQuestionGroup[] qgs) {
        this.questionGroup = qgs;
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

    public String getSurveyGroupId() {
        return surveyGroupId;
    }

    public void setSurveyGroupId(String surveyGroupId) {
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

    @Override public String toString() {
        return "Form{" +
                "questionGroups=" + Arrays.toString(questionGroup) +
                '}';
    }
}