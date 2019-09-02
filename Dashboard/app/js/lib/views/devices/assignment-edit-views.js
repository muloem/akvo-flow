import observe from '../../mixins/observe';

// removes duplicate objects with a clientId from an Ember Array

FLOW.ArrNoDupe = function (a) {
  let gotIt;
  const templ = {};
  const tempa = Ember.A([]);
  for (let i = 0; i < a.length; i++) {
    templ[a.objectAt(i).clientId] = true;
  }
  const keys = Object.keys(templ);
  for (let j = 0; j < keys.length; j++) {
    gotIt = false;
    for (let i = 0; i < a.length; i++) {
      if (a.objectAt(i).clientId == keys[j] && !gotIt) {
        tempa.pushObject(a.objectAt(i));
        gotIt = true;
      }
    }
  }
  return tempa;
};

FLOW.formatDate = function (value) {
  if (!Ember.none(value)) {
    return `${value.getFullYear()}/${value.getMonth() + 1}/${value.getDate()}`;
  } return null;
};

FLOW.AssignmentEditView = FLOW.View.extend(observe({
  'this.assignmentName': 'validateAssignmentObserver',
  'FLOW.router.navigationController.selected': 'detectChangeTab',
  'FLOW.router.devicesSubnavController.selected': 'detectChangeTab',
}), {
  devicesPreview: Ember.A([]),
  surveysPreview: Ember.A([]),
  assignmentName: null,
  language: null,

  init() {
    let startDate = null;
    let endDate = null;
    const previewDevices = Ember.A([]);
    const previewSurveys = Ember.A([]);
    this._super();
    this.set('assignmentName', FLOW.selectedControl.selectedSurveyAssignment.get('name'));
    FLOW.selectedControl.set('selectedDevices', null);
    FLOW.selectedControl.set('selectedSurveys', null);
    FLOW.selectedControl.set('selectedSurveyGroup', null);
    FLOW.selectedControl.set('selectedDeviceGroup', null);
    FLOW.surveyControl.set('content', null);
    FLOW.devicesInGroupControl.set('content', null);

    if (FLOW.selectedControl.selectedSurveyAssignment.get('startDate') > 0) {
      startDate = new Date(FLOW.selectedControl.selectedSurveyAssignment.get('startDate'));
    }
    if (FLOW.selectedControl.selectedSurveyAssignment.get('endDate') > 0) {
      endDate = new Date(FLOW.selectedControl.selectedSurveyAssignment.get('endDate'));
    }
    FLOW.dateControl.set('fromDate', FLOW.formatDate(startDate));
    FLOW.dateControl.set('toDate', FLOW.formatDate(endDate));

    this.set('language', FLOW.selectedControl.selectedSurveyAssignment.get('language'));

    const deviceIds = Ember.A(FLOW.selectedControl.selectedSurveyAssignment.get('devices'));

    deviceIds.forEach((item) => {
      previewDevices.pushObjects(FLOW.store.find(FLOW.Device, item));
    });
    this.set('devicesPreview', previewDevices);

    const surveyIds = Ember.A(FLOW.selectedControl.selectedSurveyAssignment.get('surveys'));

    surveyIds.forEach((item) => {
      if (item !== null) {
        previewSurveys.pushObjects(FLOW.store.find(FLOW.Survey, item));
      }
    });
    this.set('surveysPreview', previewSurveys);
  },

  detectChangeTab() {
    if (Ember.none(FLOW.selectedControl.selectedSurveyAssignment.get('keyId'))) {
      FLOW.selectedControl.get('selectedSurveyAssignment').deleteRecord();
    }
    FLOW.selectedControl.set('selectedSurveyAssignment', null);
  },

  assignmentNotComplete() {
    if (Ember.empty(this.get('assignmentName'))) {
      FLOW.dialogControl.set('activeAction', 'ignore');
      FLOW.dialogControl.set('header', Ember.String.loc('_assignment_name_not_set'));
      FLOW.dialogControl.set('message', Ember.String.loc('_assignment_name_not_set_text'));
      FLOW.dialogControl.set('showCANCEL', false);
      FLOW.dialogControl.set('showDialog', true);
      return true;
    }
    if (Ember.none(FLOW.dateControl.get('toDate')) || Ember.none(FLOW.dateControl.get('fromDate'))) {
      FLOW.dialogControl.set('activeAction', 'ignore');
      FLOW.dialogControl.set('header', Ember.String.loc('_date_not_set'));
      FLOW.dialogControl.set('message', Ember.String.loc('_date_not_set_text'));
      FLOW.dialogControl.set('showCANCEL', false);
      FLOW.dialogControl.set('showDialog', true);
      return true;
    }
    return false;
  },

  saveSurveyAssignment() {
    let endDateParse;
    let startDateParse;
    const devices = [];
    const surveys = [];
    if (this.assignmentNotComplete()) {
      return;
    }
    const sa = FLOW.selectedControl.get('selectedSurveyAssignment');
    sa.set('name', this.get('assignmentName'));

    if (!Ember.none(FLOW.dateControl.get('toDate'))) {
      endDateParse = Date.parse(FLOW.dateControl.get('toDate'));
    } else {
      endDateParse = null;
    }

    if (!Ember.none(FLOW.dateControl.get('fromDate'))) {
      startDateParse = Date.parse(FLOW.dateControl.get('fromDate'));
    } else {
      startDateParse = null;
    }

    sa.set('endDate', endDateParse);
    sa.set('startDate', startDateParse);
    sa.set('language', 'en');

    this.get('devicesPreview').forEach((item) => {
      devices.push(item.get('keyId'));
    });
    sa.set('devices', devices);

    this.get('surveysPreview').forEach((item) => {
      surveys.push(item.get('keyId'));
    });
    sa.set('surveys', surveys);

    FLOW.store.commit();
    // wait half a second before transitioning back to the assignments list
    setTimeout(function () {
      FLOW.router.transitionTo('navDevices.assignSurveysOverview');
    }, 500);
  },

  cancelEditSurveyAssignment() {
    if (Ember.none(FLOW.selectedControl.selectedSurveyAssignment.get('keyId'))) {
      FLOW.selectedControl.get('selectedSurveyAssignment').deleteRecord();
    }
    FLOW.selectedControl.set('selectedSurveyAssignment', null);
    FLOW.router.transitionTo('navDevices.assignSurveysOverview');
  },

  addSelectedDevices() {
    let deviceSelectors = document.getElementsByClassName('device-selector');
    let selectedDevices = [];
    FLOW.selectedControl.set('selectedDevices', []);
    for (let i = 0; i < deviceSelectors.length; i++) {
      Array.from(deviceSelectors[i].selectedOptions).map(option => option.value)
        .forEach((deviceId) => {
          let device = FLOW.deviceControl.get('content').find(d => d.get('keyId') == deviceId);
          if (device) {
            selectedDevices.push(device); // populate array of selected devices
          }
        });
    }
    FLOW.selectedControl.set('selectedDevices', selectedDevices);
    this.devicesPreview.pushObjects(FLOW.selectedControl.get('selectedDevices'));
    // delete duplicates
    this.set('devicesPreview', FLOW.ArrNoDupe(this.get('devicesPreview')));
  },

  addSelectedSurveys() {
    const sgName = FLOW.selectedControl.selectedSurveyGroup.get('code');
    FLOW.selectedControl.get('selectedSurveys').forEach((item) => {
      item.set('surveyGroupName', sgName);
    });
    this.surveysPreview.pushObjects(FLOW.selectedControl.get('selectedSurveys'));
    // delete duplicates
    this.set('surveysPreview', FLOW.ArrNoDupe(this.get('surveysPreview')));
  },

  selectAllDevices() {
    const selected = Ember.A([]);
    FLOW.devicesInGroupControl.get('content').forEach((item) => {
      selected.pushObject(item);
    });
    FLOW.selectedControl.set('selectedDevices', selected);
  },

  deselectAllDevices() {
    FLOW.selectedControl.set('selectedDevices', []);
  },

  selectAllSurveys() {
    const selected = FLOW.surveyControl.get('content').filter(item => item.get('status') === 'PUBLISHED');
    FLOW.selectedControl.set('selectedSurveys', selected);
  },

  deselectAllSurveys() {
    FLOW.selectedControl.set('selectedSurveys', []);
  },

  removeSingleSurvey(event) {
    const id = event.context.get('clientId');
    const surveysPreview = this.get('surveysPreview');
    for (let i = 0; i < surveysPreview.length; i++) {
      if (surveysPreview.objectAt(i).clientId == id) {
        surveysPreview.removeAt(i);
      }
    }
    this.set('surveysPreview', surveysPreview);
  },

  removeAllSurveys() {
    this.set('surveysPreview', Ember.A([]));
  },

  removeSingleDevice(event) {
    const id = event.context.get('clientId');
    const devicesPreview = this.get('devicesPreview');
    for (let i = 0; i < devicesPreview.length; i++) {
      if (devicesPreview.objectAt(i).clientId == id) {
        devicesPreview.removeAt(i);
      }
    }
    this.set('devicesPreview', devicesPreview);
  },

  removeAllDevices() {
    this.set('devicesPreview', Ember.A([]));
  },

  validateAssignmentObserver() {
    this.set('assignmentValidationFailure', (
      (this.assignmentName && this.assignmentName.length > 100)
      || !this.assignmentName || this.assignmentName == ''));
    if (this.assignmentName && this.assignmentName.length > 100) {
      this.set('assignmentValidationFailureReason', Ember.String.loc('_assignment_name_over_100_chars'));
    } else if (!this.assignmentName || this.assignmentName == '') {
      this.set('assignmentValidationFailureReason', Ember.String.loc('_assignment_name_not_set'));
    }
  },
});
