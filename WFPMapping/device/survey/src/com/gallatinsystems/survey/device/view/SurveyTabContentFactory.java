package com.gallatinsystems.survey.device.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TabHost.TabContentFactory;

import com.gallatinsystems.survey.device.R;
import com.gallatinsystems.survey.device.activity.SurveyViewActivity;
import com.gallatinsystems.survey.device.dao.SurveyDbAdapter;
import com.gallatinsystems.survey.device.domain.Question;
import com.gallatinsystems.survey.device.domain.QuestionGroup;
import com.gallatinsystems.survey.device.domain.QuestionResponse;
import com.gallatinsystems.survey.device.util.ConstantUtil;
import com.gallatinsystems.survey.device.util.ViewUtil;

/**
 * Creates the content for a single tab in the survey (corresponds to a
 * QuestionGroup). The tab will lay out all the questions in the QuestionGroup
 * (passed in at construction) in a List view and will append save/clear buttons
 * to the bottom of the list.
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyTabContentFactory implements TabContentFactory {

	private static final int BUTTON_WIDTH = 150;
	private QuestionGroup questionGroup;
	private SurveyViewActivity context;
	private HashMap<String, QuestionView> questionMap;
	private SurveyDbAdapter databaseAdaptor;
	private ScrollView scrollView;
	private float defaultTextSize;
	private String[] languageCodes;
	private boolean readOnly;

	public HashMap<String, QuestionView> getQuestionMap() {
		return questionMap;
	}

	/**
	 * stores the context and questionGroup to member fields
	 * 
	 * @param c
	 * @param qg
	 */
	public SurveyTabContentFactory(SurveyViewActivity c, QuestionGroup qg,
			SurveyDbAdapter dbAdaptor, float textSize, String[] languageCodes,
			boolean readOnly) {
		questionGroup = qg;
		questionMap = new HashMap<String, QuestionView>();
		context = c;
		databaseAdaptor = dbAdaptor;
		defaultTextSize = textSize;
		this.languageCodes = languageCodes;
		this.readOnly = readOnly;
	}

	/**
	 * Constructs a view using the question data from the stored questionGroup.
	 * This method makes use of a QuestionAdaptor to process individual
	 * questions.
	 */
	public View createTabContent(String tag) {
		/*
		 * vertScrollView = new ScrollView(context);
		 * vertScrollView.setLayoutParams(new
		 * LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		 */
		scrollView = new ScrollView(context);
		// vertScrollView.addView(scrollView);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		TableLayout table = new TableLayout(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		scrollView.addView(table);

		ArrayList<Question> questions = questionGroup.getQuestions();

		for (int i = 0; i < questions.size(); i++) {
			QuestionView questionView = null;
			Question q = questions.get(i);
			TableRow tr = new TableRow(context);
			tr.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			if (ConstantUtil.OPTION_QUESTION_TYPE.equalsIgnoreCase(q.getType())) {
				questionView = new OptionQuestionView(context, q,
						languageCodes, readOnly);

			} else if (ConstantUtil.FREE_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new FreetextQuestionView(context, q,
						languageCodes, readOnly);
			} else if (ConstantUtil.PHOTO_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new MediaQuestionView(context, q,
						ConstantUtil.PHOTO_QUESTION_TYPE, languageCodes,
						readOnly);
			} else if (ConstantUtil.VIDEO_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new MediaQuestionView(context, q,
						ConstantUtil.VIDEO_QUESTION_TYPE, languageCodes,
						readOnly);
			} else if (ConstantUtil.GEO_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new GeoQuestionView(context, q, languageCodes,
						readOnly);
			} else if (ConstantUtil.SCAN_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new BarcodeQuestionView(context, q,
						languageCodes, readOnly);
			} else if (ConstantUtil.TRACK_QUESTION_TYPE.equalsIgnoreCase(q
					.getType())) {
				questionView = new GeoTrackQuestionView(context, q,
						languageCodes, readOnly);
			} else {
				questionView = new QuestionView(context, q, languageCodes,
						readOnly);
			}
			questionView.setTextSize(defaultTextSize);
			questionMap.put(q.getId(), questionView);
			questionView
					.addQuestionInteractionListener((SurveyViewActivity) context);
			tr.addView(questionView);
			if (i < questions.size() - 1) {
				View ruler = new View(context);
				ruler.setBackgroundColor(0xFFFFFFFF);
				questionView.addView(ruler, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT, 2));
			}
			table.addView(tr);
		}
		// set up listeners for dependencies. Since the dependencies can span
		// groups, the parent needs to do this
		context.establishDependencies(questionGroup);

		// create save/clear buttons
		TableRow buttonRow = new TableRow(context);
		LinearLayout group = new LinearLayout(context);

		Button submitButton = new Button(context);
		submitButton.setText(R.string.submitbutton);
		submitButton.setWidth(BUTTON_WIDTH);
		group.addView(submitButton);
		if (readOnly) {
			submitButton.setEnabled(false);
		}

		Button saveButton = new Button(context);
		saveButton.setText(R.string.savebutton);
		saveButton.setWidth(BUTTON_WIDTH);
		// TODO: remove save button once we have confirmation that we want it on
		// the menu
		saveButton.setVisibility(View.GONE);
		group.addView(saveButton);
		Button clearButton = new Button(context);
		clearButton.setText(R.string.clearbutton);
		// TODO: remove clear button once we have confirmation that we want it
		// on the menu
		clearButton.setVisibility(View.GONE);
		group.addView(clearButton);
		buttonRow.addView(group);
		table.addView(buttonRow);

		// clicking save will mark the survey as saved and clear the screen
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (questionMap != null) {
					// make sure we don't lose anything that was already written
					saveState(context.getRespondentId());
					databaseAdaptor.updateSurveyStatus(context
							.getRespondentId().toString(),
							ConstantUtil.SAVED_STATUS);
					ViewUtil.showConfirmDialog(R.string.savecompletetitle,
							R.string.savecompletetext, context,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									startNewSurvey();

								}
							});
				}
			}
		});

		// clicking the clear button just blanks out all responses (across all
		// tabs)
		clearButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (questionMap != null) {
					context.resetAllQuestions();
				}
			}
		});

		// clicking submit will check to see if all mandatory questions are
		// answered and, if so, will fire a broadcast indicating that data is
		// available for transfer
		submitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (questionMap != null) {
					// make sure we don't lose anything that was already written
					saveState(context.getRespondentId());
					// get the list (across all tabs) of missing mandatory
					// responses
					ArrayList<Question> missingQuestions = context
							.checkMandatory();
					if (missingQuestions.size() == 0) {
						// if we have no missing responses, submit the survey
						databaseAdaptor.submitResponses(context
								.getRespondentId().toString());

						// send a broadcast message indicating new data is
						// available
						Intent i = new Intent(
								ConstantUtil.DATA_AVAILABLE_INTENT);
						context.sendBroadcast(i);
						ViewUtil.showConfirmDialog(
								R.string.submitcompletetitle,
								R.string.submitcompletetext, context);
						startNewSurvey();
					} else {
						// if we do have missing responses, tell the user
						ViewUtil.showConfirmDialog(R.string.cannotsave,
								R.string.mandatorywarning, context);
					}
				}
			}
		});
		loadState(context.getRespondentId());
		return scrollView;
	}

	/**
	 * creates a new response object/record and sets the id in the context then
	 * resets the question view.
	 */
	private void startNewSurvey() {
		// create a new response object so we're ready for the
		// next instance
		context.setRespondentId(databaseAdaptor.createSurveyRespondent(context
				.getSurveyId(), context.getUserId()));
		context.resetAllQuestions();
	}

	/**
	 * resets all the questions on this tab
	 */
	public void resetTabQuestions() {
		// while in general we avoid the enhanced for-loop in
		// the Android VM, we can use it here because we
		// would still need the iterator
		if (questionMap != null) {
			for (QuestionView view : questionMap.values()) {
				view.resetQuestion();
			}
			if (scrollView != null) {
				scrollView.scrollTo(0, 0);
			}
		}
	}

	/**
	 * updates the visible languages for all questions in the tab
	 * 
	 * @param langCodes
	 */
	public void updateQuestionLanguages(String[] langCodes) {
		if (questionMap != null) {
			for (QuestionView view : questionMap.values()) {
				view.updateSelectedLanguages(langCodes);
			}
		}
	}

	/**
	 * checks to make sure the mandatory questions in this tab have a response
	 * 
	 * @return
	 */
	public ArrayList<Question> checkMandatoryQuestions() {
		ArrayList<Question> missingQuestions = new ArrayList<Question>();
		// we have to check if the map is null or empty since the views aren't
		// created until the tab is clicked the first time
		if (questionMap == null || questionMap.size() == 0) {
			// add all the mandatory questions
			ArrayList<Question> uninitializedQuesitons = questionGroup
					.getQuestions();
			for (int i = 0; i < uninitializedQuesitons.size(); i++) {
				if (uninitializedQuesitons.get(i).isMandatory()) {
					missingQuestions.add(uninitializedQuesitons.get(i));
				}
			}
		} else {
			for (QuestionView view : questionMap.values()) {
				if (view.getQuestion().isMandatory()) {
					QuestionResponse resp = view.getResponse();
					if (resp == null || !resp.isValid()) {
						missingQuestions.add(view.getQuestion());
					}
				}
			}
		}
		return missingQuestions;
	}

	/**
	 * loads the state from the database using the respondentId passed in. It
	 * will then use the loaded responses to update the status of the question
	 * views in this tab.
	 * 
	 * @param respondentId
	 */
	public void loadState(Long respondentId) {
		if (respondentId != null) {
			Cursor responseCursor = databaseAdaptor
					.fetchResponsesByRespondent(respondentId.toString());
			context.startManagingCursor(responseCursor);

			while (responseCursor.moveToNext()) {
				String[] cols = responseCursor.getColumnNames();
				QuestionResponse resp = new QuestionResponse();
				for (int i = 0; i < cols.length; i++) {
					if (cols[i].equals(SurveyDbAdapter.RESP_ID_COL)) {
						resp.setId(responseCursor.getLong(i));
					} else if (cols[i]
							.equals(SurveyDbAdapter.SURVEY_RESPONDENT_ID_COL)) {
						resp.setRespondentId(responseCursor.getLong(i));
					} else if (cols[i].equals(SurveyDbAdapter.ANSWER_COL)) {
						resp.setValue(responseCursor.getString(i));
					} else if (cols[i].equals(SurveyDbAdapter.ANSWER_TYPE_COL)) {
						resp.setType(responseCursor.getString(i));
					} else if (cols[i].equals(SurveyDbAdapter.QUESTION_FK_COL)) {
						resp.setQuestionId(responseCursor.getString(i));
					}
				}
				if (questionMap != null) {
					// update the question view to reflect the loaded data
					if (questionMap.get(resp.getQuestionId()) != null) {
						questionMap.get(resp.getQuestionId()).rehydrate(resp);

					}
				}
			}
		}
	}

	/**
	 * updates text size of all questions in this tab
	 * 
	 * @param size
	 */
	public void updateTextSize(float size) {
		defaultTextSize = size;
		if (questionMap != null) {
			for (QuestionView qv : questionMap.values()) {
				qv.setTextSize(size);
			}
		}
	}

	/**
	 * persists the current question responses in this tab to the database
	 * 
	 * @param respondentId
	 */
	public void saveState(Long respondentId) {
		if (questionMap != null) {
			for (QuestionView q : questionMap.values()) {
				if (q.getResponse(true) != null
						&& q.getResponse(true).hasValue()) {
					q.getResponse(true).setRespondentId(respondentId);
					databaseAdaptor.createOrUpdateSurveyResponse(q
							.getResponse(true));
				}
			}
		}
	}
}
