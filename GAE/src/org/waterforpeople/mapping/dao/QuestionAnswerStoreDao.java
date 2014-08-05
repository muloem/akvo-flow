/*
 *  Copyright (C) 2010-2013 Stichting Akvo (Akvo Foundation)
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

package org.waterforpeople.mapping.dao;

import static com.gallatinsystems.common.util.MemCacheUtils.containsKey;
import static com.gallatinsystems.common.util.MemCacheUtils.initCache;
import static com.gallatinsystems.common.util.MemCacheUtils.putObjects;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;

import net.sf.jsr107cache.Cache;

import org.waterforpeople.mapping.domain.QuestionAnswerStore;

import com.gallatinsystems.framework.dao.BaseDAO;
import com.gallatinsystems.framework.servlet.PersistenceFilter;

public class QuestionAnswerStoreDao extends BaseDAO<QuestionAnswerStore> {

    private Cache cache;

    public QuestionAnswerStoreDao() {
        super(QuestionAnswerStore.class);
        cache = initCache(4 * 60 * 60); // cache questions list for 4 hours
    }

    public List<QuestionAnswerStore> listBySurvey(Long surveyId) {
        return super.listByProperty("surveyId", surveyId, "Long");

    }

    public List<QuestionAnswerStore> listByQuestion(Long questionId) {
        return super.listByProperty("questionID", questionId.toString(),
                "String", "createdDateTime");
    }

    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByQuestion(Long questionId, String cursor, Integer pageSize) {
        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

        StringBuilder filterString = new StringBuilder();
        StringBuilder paramString = new StringBuilder();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        appendNonNullParam("questionID", filterString, paramString, "String",
                String.valueOf(questionId),
                paramMap);

        query.setFilter(filterString.toString());
        query.declareParameters(paramString.toString());
        query.setOrdering("createdDateTime");
        prepareCursor(cursor, pageSize, query);
        return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
    }

    /**
     * lists all the QuestionAnswerStore objects that match the type passed in
     *
     * @param type
     * @param cursor
     * @param pageSize
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByTypeAndDate(String type,
            Long surveyId, Date sinceDate, String cursor, Integer pageSize) {

        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

        Map<String, Object> paramMap = null;

        StringBuilder filterString = new StringBuilder();
        StringBuilder paramString = new StringBuilder();
        paramMap = new HashMap<String, Object>();

        appendNonNullParam("type", filterString, paramString, "String", type,
                paramMap);
        appendNonNullParam("surveyId", filterString, paramString, "Long",
                surveyId, paramMap);
        appendNonNullParam("lastUpdateDateTime", filterString, paramString,
                "Date", sinceDate, paramMap, GTE_OP);
        if (sinceDate != null) {
            query.declareImports("import java.util.Date");
        }
        query.setFilter(filterString.toString());
        query.declareParameters(paramString.toString());
        prepareCursor(cursor, pageSize, query);
        return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
    }

    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByTypeValue(String type, String value) {
        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

        Map<String, Object> paramMap = null;

        StringBuilder filterString = new StringBuilder();
        StringBuilder paramString = new StringBuilder();
        paramMap = new HashMap<String, Object>();

        appendNonNullParam("type", filterString, paramString, "String", type,
                paramMap);
        appendNonNullParam("value", filterString, paramString, "String", value,
                paramMap);
        query.setFilter(filterString.toString());
        query.declareParameters(paramString.toString());
        return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
    }

    /**
     * lists all the QuestionAnswerStore objects that match the type passed in
     *
     * @param sinceDate
     * @param cursor
     * @param pageSize
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByNotNullCollectionDateBefore(
            Date sinceDate, String cursor, Integer pageSize) {

        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

        Map<String, Object> paramMap = null;

        StringBuilder filterString = new StringBuilder();
        StringBuilder paramString = new StringBuilder();
        paramMap = new HashMap<String, Object>();
        appendNonNullParam("collectionDate", filterString, paramString, "Date",
                sinceDate, paramMap, LTE_OP);
        if (sinceDate != null) {
            query.declareImports("import java.util.Date");
        }
        query.setFilter(filterString.toString());
        query.declareParameters(paramString.toString());
        prepareCursor(cursor, pageSize, query);
        return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
    }

    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByNotNullCollectionDateAfter(
            Date sinceDate, String cursor, Integer pageSize) {

        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

        Map<String, Object> paramMap = null;

        StringBuilder filterString = new StringBuilder();
        StringBuilder paramString = new StringBuilder();
        paramMap = new HashMap<String, Object>();
        appendNonNullParam("collectionDate", filterString, paramString, "Date",
                sinceDate, paramMap, GTE_OP);
        if (sinceDate != null) {
            query.declareImports("import java.util.Date");
        }
        query.setFilter(filterString.toString());
        query.declareParameters(paramString.toString());
        // prepareCursor(cursor, pageSize, query);
        log.log(Level.INFO, query.toString());
        return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
    }

    /**
     * lists all the QuestionAnswerStore objects that match the type passed in
     *
     * @param sinceDate
     * @param cursor
     * @param pageSize
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listByExactDateString() {

        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery("select from "
                + QuestionAnswerStore.class.getName()
                + " where collectionDate == 5674906531303000000");
        log.log(Level.INFO, query.toString());
        return (List<QuestionAnswerStore>) query.execute();
    }

    @SuppressWarnings("unchecked")
    public List<QuestionAnswerStore> listBySurveyInstance(Long surveyInstanceId) {
        List<QuestionAnswerStore> responses = super.listByProperty("surveyInstanceId", surveyInstanceId, "Long");
        cache(responses);
        return responses;
    }

    @SuppressWarnings("unchecked")
    public QuestionAnswerStore getByQuestionAndSurveyInstance(Long questionId, Long instanceId) {
        String cacheKey = getCacheKey(instanceId + "-" + questionId);
        if (containsKey(cache, cacheKey)) {
            return (QuestionAnswerStore) cache.get(cacheKey);
        }

        PersistenceManager pm = PersistenceFilter.getManager();
        javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);
        query.setFilter("surveyInstanceId == surveyInstanceIdParam && questionID == questionIdParam");
        query.declareParameters("Long surveyInstanceIdParam, String questionIdParam");
        List<QuestionAnswerStore> results = (List<QuestionAnswerStore>) query.execute(
                instanceId, questionId.toString());
        if (results != null && results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public boolean isCached(Long questionId, Long instanceId) {
        return containsKey(cache, getCacheKey(instanceId + "-" + questionId));
    }

    /**
     * Saves response and update cache
     *
     * @param reponse
     */
    public QuestionAnswerStore save(QuestionAnswerStore reponse) {
        // first save and get Id
        QuestionAnswerStore savedResponse = super.save(reponse);
        cache(Arrays.asList(savedResponse));
        return savedResponse;
    }

    /**
     * Save a collection of responses and cache
     *
     * @param responseList
     * @return
     */
    public List<QuestionAnswerStore> save(List<QuestionAnswerStore> responseList) {
        List<QuestionAnswerStore> savedResponses = (List<QuestionAnswerStore>) super
                .save(responseList);
        cache(savedResponses);
        return savedResponses;
    }

    /**
     * Delete from cache and datastore
     *
     * @param response
     */
    public void delete(QuestionAnswerStore response) {
        uncache(Arrays.asList(response));
        super.delete(response);
    }

    /**
     * Delete response list from cache and datastore
     *
     * @param responsesList
     */
    public void delete(List<QuestionAnswerStore> responsesList) {
        uncache(responsesList);
        super.delete(responsesList);
    }

    /**
     * Add a collection of QuestionAnswerStore objects to the cache
     *
     * @param responseList
     */
    private void cache(List<QuestionAnswerStore> responseList) {
        if (responseList == null || responseList.isEmpty()) {
            return;
        }

        Map<Object, Object> cacheMap = new HashMap<Object, Object>();
        for (QuestionAnswerStore response : responseList) {
            String cacheKey = getCacheKey(response.getSurveyInstanceId() + "-"
                    + response.getQuestionID());
            cacheMap.put(cacheKey, response);
        }

        putObjects(cache, cacheMap);
    }

    /**
     * Remove a collection of responses from the cache
     *
     * @param responsesList
     */
    private void uncache(List<QuestionAnswerStore> responsesList) {
        if (responsesList == null || responsesList.isEmpty()) {
            return;
        }

        for (QuestionAnswerStore response : responsesList) {
            String cacheKey = getCacheKey(response.getSurveyInstanceId() + "-"
                    + response.getQuestionID());
            if (containsKey(cache, cacheKey)) {
                cache.remove(cacheKey);
            }
        }
    }
}
