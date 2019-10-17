var patientqueue = patientqueue || {};

patientqueue.completePatientQueueDialog = null;
patientqueue.patientId = null;


patientqueue.showCompletePatientQueueDialog = function (patientId) {
    patientqueue.patientId = patientId;
    if (patientqueue.completePatientQueueDialog == null) {
        patientqueue.createCompletePatientQueueDialog();
    }
    if(patientQueueId!==""){
        patientqueue.completePatientQueueDialog.show();
    }else {
        jq().toastmessage('showErrorToast', "Patient doesnt have an active  queue");
    }

};

patientqueue.closeDialog = function () {
    patientqueue.completePatientQueueDialog.close();
};

patientqueue.createCompletePatientQueueDialog = function () {
    patientqueue.completePatientQueueDialog = emr.setupConfirmationDialog({
        selector: '#complete_patient_queue',
        actions: {
            cancel: function () {
                patientqueue.completePatientQueueDialog.close();
            }
        }
    });

    patientqueue.completePatientQueueDialog.close();
};