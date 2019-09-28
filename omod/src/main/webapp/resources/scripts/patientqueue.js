var patientqueue = patientqueue || {};

patientqueue.completePatientQueueDialog = null;
patientqueue.patientId = null;
patientqueue.sendPatientQueueDialog = null;


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

patientqueue.showSendPatientQueueDialog = function (patientId) {
    patientqueue.patientId = patientId;
    if (patientqueue.sendPatientQueueDialog == null) {
        patientqueue.createSendPatientQueueDialog();
    }
    jq("#patient_id").val(patientqueue.patientId);
    patientqueue.sendPatientQueueDialog.show();
};

patientqueue.closeSendPatientQueueDialog = function () {
    patientqueue.sendPatientQueueDialog.close();
};


patientqueue.createSendPatientQueueDialog = function () {
    patientqueue.sendPatientQueueDialog = emr.setupConfirmationDialog({
        selector: '#send_patient_to_queue_dialog',
        actions: {
            cancel: function () {
                patientqueue.sendPatientQueueDialog.close();
            }
        }
    });

    patientqueue.sendPatientQueueDialog.show();
}