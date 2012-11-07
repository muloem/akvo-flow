// ************************ Surveys *************************

FLOW.SurveySidebarView = Ember.View.extend({
	surveyTitle:null,
	surveyDescription: null,
	surveyTypeControl:null,
	surveySectorTypeControl:null,

	init: function(){
		this._super();
		this.set('surveyTitle',FLOW.selectedControl.selectedSurvey.get('name'));
		this.set('surveyDescription',FLOW.selectedControl.selectedSurvey.get('description'));
		this.set('typeTypeControl',FLOW.selectedControl.selectedSurvey.get('pointType'));
		
		// TODO imlement sector codes on surveys
		//surveySectorTypeControl=FLOW.selectedControl.selectedSurvey.get('sector');
	},

	doSaveSurvey:function(){
		var sgId=FLOW.selectedControl.selectedSurvey.get('id');
		var survey=FLOW.store.find(FLOW.Survey, sgId);
		survey.set('name',this.get('surveyTitle'));
		survey.set('description',this.get('surveyDescription'));
		survey.set('pointType',this.get('surveyTypeControl'));
		FLOW.store.commit();
		//FLOW.selectedControl.set('selectedSurvey',FLOW.store.find(FLOW.Survey, sgId));
	},

	doPreviewSurvey:function(){
		console.log("TODO: implement preview survey");
	},

	doPublishSurvey:function(){
		console.log("TODO: implement publish survey");
	}
});


FLOW.QuestionGroupItemView = Ember.View.extend({
	content: null, // question group content comes through binding in handlebars file
	zeroItem: false,
	renderView:false,
	showQGDeleteDialogue:false,

	amVisible: function() {
		var selected = FLOW.selectedControl.get('selectedQuestionGroup');
		if (selected) {

			var isVis = (this.content.get('keyId') === FLOW.selectedControl.selectedQuestionGroup.get('keyId'));
			return isVis;
		} else {
			return null;
		}
	}.property('FLOW.selectedControl.selectedQuestionGroup', 'content.keyId').cacheable(),

	toggleVisibility: function() {
		if (this.get('amVisible')) {
			FLOW.selectedControl.set('selectedQuestionGroup', null);
		} else {
			FLOW.selectedControl.set('selectedQuestionGroup', this.content);
		}
	},

	showHideText: function() {
		return this.get('amVisible') ? 'Hide questions' : 'Show questions';
	}.property('amVisible').cacheable(),

	doQGroupNameEdit:function(){
		console.log("TODO - group name edit");
	},

	// true if one question group has been selected for Move
	oneSelectedForMove: function() {
		var selectedForMove = FLOW.selectedControl.get('selectedForMoveQuestionGroup');
		if (selectedForMove) {
			return true;
		} else {
			return false;
		}
	}.property('FLOW.selectedControl.selectedForMoveQuestionGroup'),

	// true if one question group has been selected for Copy
	oneSelectedForCopy: function() {
		var selectedForCopy = FLOW.selectedControl.get('selectedForCopyQuestionGroup');
		if (selectedForCopy) {
			return true;
		} else {
			return false;
		}
	}.property('FLOW.selectedControl.selectedForCopyQuestionGroup'),

	
	// show delete QGroup dialogue
	showQGroupDeleteDialogue:function(){
		this.set('showQGDeleteDialogue',true);
	},

	// cancel question group delete
	cancelQGroupDelete:function(){
		this.set('showQGDeleteDialogue',false);
	},

	// execure group delete
	doQGroupDelete:function(){
		// TODO show popup
		// if cancel: remove popup, don't do anything
		// if delete: remove question group.
		this.set('showQGDeleteDialogue',false);
		
		var qgDeleteOrder = this.content.get('order');
		var qgDeleteId = this.content.get('keyId');
				
		FLOW.questionGroupControl.get('content').forEach(function(item){
			var currentOrder=item.get('order');
			
			if (currentOrder==qgDeleteOrder){
				var questionGroup = FLOW.store.find(FLOW.QuestionGroup, qgDeleteId);
				questionGroup.deleteRecord();
				FLOW.store.commit();
			}
			else if (currentOrder>qgDeleteOrder){
				item.set('order',item.get('order')-1);
			}
		}); // end of forEach



		FLOW.questionGroupControl.set('content',FLOW.store.findAll(FLOW.QuestionGroup));
		
		
		// TODO: implement persistence
		// TODO: solve "could not respond to event didChangeData in state rootState.deleted.saved." error.

	},

	// prepare for group copy. Shows 'copy to here' buttons
	doQGroupCopy:function(){
		FLOW.selectedControl.set('selectedForCopyQuestionGroup', this.content);
	},

	// cancel group copy
	doQGroupCopyCancel:function(){
		FLOW.selectedControl.set('selectedForCopyQuestionGroup', null);
	},

	// prepare for group move. Shows 'move here' buttons
	doQGroupMove:function(){
		FLOW.selectedControl.set('selectedForMoveQuestionGroup', this.content);
	},

	// cancel group move
	doQGroupMoveCancel:function(){
		FLOW.selectedControl.set('selectedForMoveQuestionGroup', null);
	},

	// execture group move to selected location
	doQGroupMoveHere:function(){
		
		var selectedOrder = FLOW.selectedControl.selectedForMoveQuestionGroup.get('order');
		var insertAfterOrder;
		var movingUp=false;

		if (this.get('zeroItem')) {insertAfterOrder=0;} else {insertAfterOrder=this.content.get('order');}

		FLOW.questionGroupControl.propertyWillChange('content');

		// moving to the same place => do nothing
		if ((selectedOrder==insertAfterOrder)||(selectedOrder==(insertAfterOrder+1))){}
		else {
			// determine if the item is moving up or down
			movingUp = (selectedOrder<insertAfterOrder);
		
			FLOW.questionGroupControl.get('content').forEach(function(item){
				var currentOrder=item.get('order');

				// item moving up
				if (movingUp) {
					// if outside of change region, do not move
					if ((currentOrder<selectedOrder) || (currentOrder>insertAfterOrder)){ }

					// move moving item to right location
					else if (currentOrder==selectedOrder) {	item.set('order',insertAfterOrder); }
					
					// move rest down
					else { item.set('order',item.get('order')-1); }
				}

				// item moving down
				else {
					if ((currentOrder<=insertAfterOrder) || (currentOrder>selectedOrder)){ }

					else if (currentOrder==selectedOrder) {	item.set('order',insertAfterOrder+1); }

					else {	item.set('order',item.get('order')+1); }
				}
			}); // end of forEach
		} // end of top else
		
		FLOW.store.commit();
		FLOW.questionGroupControl.propertyDidChange('content');
		FLOW.selectedControl.set('selectedForMoveQuestionGroup', null);
	},

	// execure group copy to selected location
	doQGroupCopyHere:function(){
		
		var selectedOrder = FLOW.selectedControl.selectedForCopyQuestionGroup.get('order');
		var insertAfterOrder;

		if (this.get('zeroItem')) {insertAfterOrder=0;} else {insertAfterOrder=this.content.get('order');}
		console.log("selected, insertAfter: "+selectedOrder+", "+insertAfterOrder);
		// move up to make space
		FLOW.questionGroupControl.get('content').forEach(function(item){
			var currentOrder=item.get('order');
			if (currentOrder>insertAfterOrder) {item.set('order',item.get('order')+1);
				console.log("upping "+currentOrder);
			}
		}); // end of forEach
	
		// create copy of QuestionGroup item in the store
		var newRec = FLOW.store.createRecord(FLOW.QuestionGroup,{
			"description": FLOW.selectedControl.selectedForCopyQuestionGroup.get('description'),
			"order":insertAfterOrder+1,
			"name":FLOW.selectedControl.selectedForCopyQuestionGroup.get('name'),
			"surveyId":FLOW.selectedControl.selectedForCopyQuestionGroup.get('surveyId'),
			"displayName":FLOW.selectedControl.selectedForCopyQuestionGroup.get('displayName')});
		
		FLOW.store.commit();
		
		// TODO implement commit to persistence layer
		// TODO create copy of questions contained in QuestionGroup and insert them in the store

		FLOW.questionGroupControl.set('content',FLOW.store.findAll(FLOW.QuestionGroup)); // only loads already loaded models
		
		FLOW.selectedControl.set('selectedForCopyQuestionGroup', null);
	}

}); // end QuestionGroupItemView



FLOW.QuestionView = Ember.View.extend({
	content:null,
	questionName:null,
	checkedMandatory: false,
	checkedDependent: false,
	checkedOptionMultiple:false,
	checkedOptionOther:false,
	selectedQuestionType:null,
	selectedOptionEdit:null,
	
	amOpenQuestion: function() {
		var selected = FLOW.selectedControl.get('selectedQuestion');
		if (selected) {

			var isOpen = (this.content.get('keyId') == FLOW.selectedControl.selectedQuestion.get('keyId'));
			return isOpen;
		} else {
			return false;
		}
	}.property('FLOW.selectedControl.selectedQuestion', 'content.keyId').cacheable(),

	
	amOptionType:function() {
		if (this.selectedQuestionType){ return (this.selectedQuestionType.get('value')=='option') ? true : false;}
		else {return false;}
	}.property('this.selectedQuestionType').cacheable(),
	
	amNumberType:function() {
		if (this.selectedQuestionType){ return (this.selectedQuestionType.get('value')=='number') ? true : false;}
		else {return false;}
	}.property('this.selectedQuestionType').cacheable(),
		
	doEdit: function() {
		FLOW.selectedControl.set('selectedQuestion', this.content);
	
		this.set('questionName',FLOW.selectedControl.selectedQuestion.get('displayName'));
		
		//FLOW.optionControl.set('editCopy',FLOW.optionControl.get('questionOptionsList'));
	
		//TODO populate selected question type
		//TODO populate tooltip
		//TODO populate question options
		//TODO populate help
		//TODO populate translations
	},
	
	doCancelEditQuestion: function() {
		FLOW.selectedControl.set('selectedQuestion', null);
		console.log('canceling edit');
	},
	
	doSaveEditQuestion: function() {
	},
	
	doCopy: function() {
		console.log("doing doDuplicate");
	},
	
	doMove: function() {
			console.log("doing doMove");
	},
	
	doDelete: function() {
			console.log("doing doDelete");
	}
});