package com.gallatinsystems.survey.dao;

import java.util.List;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto.QuestionType;

import com.gallatinsystems.framework.dao.BaseDAO;
import com.gallatinsystems.framework.servlet.PersistenceFilter;
import com.gallatinsystems.survey.domain.Question;
import com.google.appengine.api.datastore.Key;

public class QuestionDao extends BaseDAO<Question> {

	private QuestionOptionDao optionDao;
	private QuestionHelpMediaDao helpDao;

	public QuestionDao() {
		super(Question.class);
		optionDao = new QuestionOptionDao();
		helpDao = new QuestionHelpMediaDao();
	}

	public List<Question> listQuestionByType(QuestionType type) {
		return listByProperty("type", type.toString(), "String");
	}

	/**
	 * loads the Question object but NOT any associated options
	 * 
	 * @param id
	 * @return
	 */
	public Question getQuestionHeader(Long id) {
		return super.getByKey(id);

	}

	public void delete(Question question, Long questionGroupId) {
		super.delete(question);

	}

	public Question save(Question question, Long questionGroupId) {
		question = super.save(question);
		return question;
	}

	public Question findByReferenceId(String refid) {
		Question q = findByProperty("referenceIndex", refid, "String");
		return q;
	}

	public Question save(Question question) {
		question = super.save(question);
		return question;
	}

	public Question getByKey(Long id, boolean needDetails) {
		Question q = getByKey(id);
		if (needDetails) {
			q.setQuestionHelpMediaMap(helpDao.listHelpByQuestion(q.getKey()
					.getId()));
			if (Question.Type.OPTION == q.getType()) {
				q.setQuestionOptionMap(optionDao.listOptionByQuestion(q
						.getKey().getId()));
			}
		}
		return q;
	}

	public Question getByKey(Key key) {
		return super.getByKey(key);
	}

	public TreeMap<Integer, Question> listQuestionsByQuestionGroup(
			Long questionGroupId, boolean needDetails) {
		List<Question> qList = listByProperty("questionGroupId",
				questionGroupId, "Long");
		TreeMap<Integer, Question> map = new TreeMap<Integer, Question>();
		if (qList != null) {
			int i = 1;
			for (Question q : qList) {
				map.put(q.getOrder() != null ? q.getOrder() : i, q);
				i++;
				if (needDetails) {
					q.setQuestionHelpMediaMap(helpDao.listHelpByQuestion(q
							.getKey().getId()));
					if (Question.Type.OPTION == q.getType()) {
						q.setQuestionOptionMap(optionDao.listOptionByQuestion(q
								.getKey().getId()));
					}
				}
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public Question getByPath(Integer order, String path) {
		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(Question.class);
		query.setFilter(" path == pathParam && order == orderParam");
		query.declareParameters("String pathParam, String orderParam");
		List<Question> results = (List<Question>) query.execute(path, order);
		if (results != null && results.size() > 0) {
			return results.get(0);
		} else {
			return null;
		}
	}

}
