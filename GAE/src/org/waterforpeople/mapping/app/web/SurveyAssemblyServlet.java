package org.waterforpeople.mapping.app.web;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto.QuestionType;
import org.waterforpeople.mapping.app.web.dto.SurveyAssemblyRequest;
import org.waterforpeople.mapping.dao.SurveyContainerDao;

import com.gallatinsystems.framework.rest.AbstractRestApiServlet;
import com.gallatinsystems.framework.rest.RestRequest;
import com.gallatinsystems.framework.rest.RestResponse;
import com.gallatinsystems.survey.dao.QuestionDao;
import com.gallatinsystems.survey.dao.QuestionQuestionGroupAssocDao;
import com.gallatinsystems.survey.dao.SurveyQuestionGroupAssocDao;
import com.gallatinsystems.survey.dao.SurveyXMLFragmentDao;
import com.gallatinsystems.survey.domain.OptionContainer;
import com.gallatinsystems.survey.domain.Question;
import com.gallatinsystems.survey.domain.QuestionOption;
import com.gallatinsystems.survey.domain.QuestionQuestionGroupAssoc;
import com.gallatinsystems.survey.domain.SurveyContainer;
import com.gallatinsystems.survey.domain.SurveyQuestionGroupAssoc;
import com.gallatinsystems.survey.domain.SurveyXMLFragment;
import com.gallatinsystems.survey.domain.SurveyXMLFragment.FRAGMENT_TYPE;
import com.gallatinsystems.survey.domain.xml.Dependency;
import com.gallatinsystems.survey.domain.xml.ObjectFactory;
import com.gallatinsystems.survey.domain.xml.Option;
import com.gallatinsystems.survey.domain.xml.Options;
import com.gallatinsystems.survey.domain.xml.Tip;
import com.gallatinsystems.survey.domain.xml.ValidationRule;
import com.gallatinsystems.survey.xml.SurveyXMLAdapter;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;

public class SurveyAssemblyServlet extends AbstractRestApiServlet {
	private static final Logger log = Logger
			.getLogger(SurveyAssemblyServlet.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = -6044156962558183224L;

	@Override
	protected RestRequest convertRequest() throws Exception {
		HttpServletRequest req = getRequest();
		RestRequest restRequest = new SurveyAssemblyRequest();
		restRequest.populateFromHttpRequest(req);
		return restRequest;
	}

	@Override
	protected RestResponse handleRequest(RestRequest req) throws Exception {
		RestResponse response = new RestResponse();
		SurveyAssemblyRequest importReq = (SurveyAssemblyRequest) req;
		if (SurveyAssemblyRequest.ASSEMBLE_SURVEY.equalsIgnoreCase(importReq
				.getAction())) {
			assembleSurvey(importReq.getSurveyId());
		} else if (SurveyAssemblyRequest.DISPATCH_ASSEMBLE_QUESTION_GROUP
				.equalsIgnoreCase(importReq.getAction())) {
			this.dispatchAssembleQuestionGroup(importReq.getSurveyId(),
					importReq.getQuestionGroupId(), importReq
							.getLastGroupFlag());
		} else if (SurveyAssemblyRequest.ASSEMBLE_QUESTION_GROUP
				.equalsIgnoreCase(importReq.getAction())) {
			assembleQuestionGroups(importReq.getSurveyId());
		}

		return null;
	}

	@Override
	protected void writeOkResponse(RestResponse resp) throws Exception {
		// TODO Auto-generated method stub

	}

	private void assembleSurvey(Long surveyId) {

		/**************
		 * 1, Select survey based on surveyId 2. Retrieve all question groups
		 * fire off queue tasks
		 */
		SurveyQuestionGroupAssocDao sqgadao = new SurveyQuestionGroupAssocDao();
		List<SurveyQuestionGroupAssoc> sqgaList = sqgadao
				.listBySurveyId(surveyId);
		ArrayList<Long> questionGroupIdList = new ArrayList<Long>();
		for (SurveyQuestionGroupAssoc item : sqgaList)
			questionGroupIdList.add(item.getQuestionGroupId());
		int count = 0;
		Boolean lastGroupFlag = false;
		for (Long item : questionGroupIdList) {
			Queue surveyAssemblyQueue = QueueFactory.getQueue("surveyAssembly");

			if (count == (questionGroupIdList.size() - 1))
				lastGroupFlag = true;
			surveyAssemblyQueue.add(url("/app_worker/surveyassembly").param(
					"action",
					SurveyAssemblyRequest.DISPATCH_ASSEMBLE_QUESTION_GROUP)
					.param("surveyId", surveyId.toString()).param(
							"questionGroupId", item.toString()).param(
							"lastGroupFlag", lastGroupFlag.toString()));
			count++;
		}

	}

	private void dispatchAssembleQuestionGroup(Long surveyId,
			Long questionGroupId, Boolean lastGroupFlag) {
		QuestionQuestionGroupAssocDao qgqaDao = new QuestionQuestionGroupAssocDao();
		List<QuestionQuestionGroupAssoc> qqgaList = qgqaDao
				.listByQuestionGroupId(questionGroupId);
		QuestionDao questionDao = new QuestionDao();
		StringBuilder sb = new StringBuilder();
		int count = 0;
		int i = 0;

		/*
		 * for (count = startRow; count < qqgaList.size() && i < 10; count++) {
		 * i++; // get the question list // process 10 records and save then
		 * spawn new queue job Question q =
		 * questionDao.getByKey(qqgaList.get(count) .getQuestionId());
		 * sb.append(marshallQuestion(q)); }
		 */

		for (QuestionQuestionGroupAssoc item : qqgaList) {
			Question q = questionDao.getByKey(item.getQuestionId());
			sb.append(marshallQuestion(q));
			count++;
		}
		SurveyXMLFragment sxf = new SurveyXMLFragment();
		sxf.setSurveyId(surveyId);
		sxf.setQuestionGroupId(questionGroupId);
		// sxf.setFragmentOrder(startRow / 10);
		sxf.setFragment(new Text(sb.toString()));
		sxf.setFragmentType(FRAGMENT_TYPE.QUESTION);
		SurveyXMLFragmentDao sxmlfDao = new SurveyXMLFragmentDao();
		sxmlfDao.save(sxf);
		if (lastGroupFlag) {
			// Assemble the fragments
			Queue surveyAssemblyQueue = QueueFactory.getQueue("surveyAssembly");
			surveyAssemblyQueue.add(url("/app_worker/surveyassembly").param(
					"action", SurveyAssemblyRequest.ASSEMBLE_QUESTION_GROUP)
					.param("surveyId", surveyId.toString()));
		}
	}

	public static final String FREE_QUESTION_TYPE = "free";
	public static final String OPTION_QUESTION_TYPE = "option";
	public static final String GEO_QUESTION_TYPE = "geo";
	public static final String VIDEO_QUESTION_TYPE = "video";
	public static final String PHOTO_QUESTION_TYPE = "photo";
	public static final String SCAN_QUESTION_TYPE = "scan";

	private String marshallQuestion(Question q) {

		SurveyXMLAdapter sax = new SurveyXMLAdapter();
		ObjectFactory objFactory = new ObjectFactory();
		com.gallatinsystems.survey.domain.xml.Question qXML = objFactory
				.createQuestion();
		qXML.setId(new String("" + q.getKey().getId() + ""));
		// ToDo fix
		qXML.setMandatory("false");
		if (q.getText() != null) {
			com.gallatinsystems.survey.domain.xml.Text t = new com.gallatinsystems.survey.domain.xml.Text();
			t.setContent(q.getText());
			qXML.setText(t);
		}
		if (q.getTip() != null) {
			Tip tip = new Tip();
			tip.setContent(q.getTip());
			qXML.setTip(tip);
		}

		if (q.getValidationRule() != null) {
			ValidationRule validationRule = objFactory.createValidationRule();

			// ToDo set validation rule xml
			// validationRule.setAllowDecimal(value)
		}

		// ToDo marshall xml
		// qXML.setText(q.getText());

		if (q.getType().equals(QuestionType.FREE_TEXT))
			qXML.setType(FREE_QUESTION_TYPE);
		else if (q.getType().equals(QuestionType.GEO))
			qXML.setType(GEO_QUESTION_TYPE);
		else if (q.getType().equals(QuestionType.NUMBER)) {
			qXML.setType(FREE_QUESTION_TYPE);
			ValidationRule vrule = new ValidationRule();
			vrule.setValidationType("numeric");
			vrule.setSigned("false");
			qXML.setValidationRule(vrule);
		} else if (q.getType().equals(QuestionType.OPTION))
			qXML.setType(OPTION_QUESTION_TYPE);
		else if (q.getType().equals(QuestionType.PHOTO))
			qXML.setType(PHOTO_QUESTION_TYPE);
		else if (q.getType().equals(QuestionType.VIDEO))
			qXML.setType(VIDEO_QUESTION_TYPE);
		else if (q.getType().equals(QuestionType.SCAN))
			qXML.setType(SCAN_QUESTION_TYPE);

		if (q.getKey() != null)
			qXML.setOrder(q.getKey().toString());
		// ToDo set dependency xml
		Dependency dependency = objFactory.createDependency();
		if (q.getDependQuestion() != null) {
			dependency.setQuestion(q.getDependQuestion().getQuestionId()
					.toString());
			dependency.setAnswerValue(q.getDependQuestion().getAnswerValue());
			qXML.setDependency(dependency);
		}

		if (q.getOptionContainer() != null) {
			OptionContainer oc = q.getOptionContainer();
			// System.out.println("			OptionContainer: " +
			// oc.getKey().getId()
			// + ":" + oc.getAllowMultipleFlag() + ":"
			// + oc.getAllowOtherFlag());
			Options options = objFactory.createOptions();
			// if(oc.getAllowMultipleFlag()!=null)
			// options.setAllowMultiple()
			if (oc.getAllowOtherFlag() != null)
				options.setAllowOther(oc.getAllowOtherFlag().toString());

			if (oc.getOptionsList() != null) {
				ArrayList<Option> optionList = new ArrayList<Option>();
				// System.out.println("				ocList size:" +
				// optionList.size());
				for (QuestionOption qo : oc.getOptionsList()) {
					// System.out.println("						option:" +
					// qo.getKey().getId()
					// + ":" + qo.getCode() + ":"
					// + qo.getText());
					Option option = objFactory.createOption();
					option.setContent(qo.getText());
					option.setValue(qo.getCode());
					optionList.add(option);
				}
				options.setOptionList(optionList);
			}
			qXML.setOptions(options);
		}
		com.gallatinsystems.survey.domain.xml.Question questionXML = objFactory
				.createQuestion();
		String questionDocument = null;
		try {
			questionDocument = sax.marshal(questionXML);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		questionDocument = questionDocument
				.replace(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
						"");
		log.info(questionDocument);
		return questionDocument;
	}

	private void assembleQuestionGroups(Long surveyId) {
		SurveyXMLFragmentDao sxmlfDao = new SurveyXMLFragmentDao();
		List<SurveyXMLFragment> sxmlfList = sxmlfDao.listSurveyFragments(
				surveyId, SurveyXMLFragment.FRAGMENT_TYPE.QUESTION_GROUP);
		StringBuilder sbQG = new StringBuilder();
		for (SurveyXMLFragment item : sxmlfList) {
			sbQG.append(item.getFragment().toString());
		}
		StringBuilder completeSurvey = new StringBuilder();
		String surveyHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><survey>";
		String surveyFooter = "</survey>";
		completeSurvey.append(surveyHeader);
		completeSurvey.append(sbQG.toString());
		sbQG = null;
		completeSurvey.append(surveyFooter);
		log.info(completeSurvey.toString());
		SurveyContainer sc = new SurveyContainer();
		sc.setSurveyDocument(new Text(completeSurvey.toString()));
		sc.setSurveyId(surveyId);
		SurveyContainerDao scDao = new SurveyContainerDao();
		scDao.save(sc);
	}
}