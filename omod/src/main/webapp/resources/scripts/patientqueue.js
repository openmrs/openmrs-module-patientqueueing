var patientqueue = patientqueue || {};

patientqueue.completeQueueDialog = null;
patientqueue.addPatientQueueDialog = null;
patientqueue.readMessageDialog = null;
patientqueue.alert_message_id=null;
patientqueue.message=null;
patientqueue.patientId=null;
patientqueue.createMessageDialog=null;


patientqueue.showCompleteQueueDialog = function (patientId) {
    patientqueue.patientId = patientId;
    if (patientqueue.completeQueueDialog == null) {
        patientqueue.createCompletePatientQueueDialog();
    }
    patientqueue.completeQueueDialog.show();
};

patientqueue.closeDialog = function () {
    patientqueue.completeQueueDialog.close();
};


patientqueue.createCompletePatientQueueDialog = function () {
    patientqueue.completeQueueDialog = emr.setupConfirmationDialog({
        selector: '#complete_patient_queue',
        actions: {
            confirm: function () {
                emr.getFragmentActionWithCallback('patientqueueing', 'completePatientQueue', 'post', visit.buildVisitTypeAttributeParams(),
                    function (v) {
                        jq('#complete_patient_queue' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        patientqueue.goToReturnUrl(patientqueue.patientId, v);
                    });
            },
            cancel: function () {
                patientqueue.completeQueueDialog.close();
            }
        }
    });

    patientqueue.completeQueueDialog.close();
}


patientqueue.showAddPatientQueueDialog = function (patientId) {
    patientqueue.patientId = patientId;
    if (patientqueue.addPatientQueueDialog == null) {
        patientqueue.createAddPatientQueueDialog();
    }
    jq("#patient_id").val(patientqueue.patientId);
    patientqueue.addPatientQueueDialog.show();
};

patientqueue.closeAddpatientQueueDialog = function () {
    patientqueue.addPatientQueueDialog.close();
};


patientqueue.createAddPatientQueueDialog = function () {
    patientqueue.addPatientQueueDialog = emr.setupConfirmationDialog({
        selector: '#add_patient_to_queue_dialog',
        actions: {
            cancel: function () {
                patientqueue.addPatientQueueDialog.close();
            }
        }
    });

    patientqueue.addPatientQueueDialog.close();
}


patientqueue.showReadMessageDialog = function (message,alert_message_id) {
    patientqueue.message = message;
    patientqueue.alert_message_id=alert_message_id;
    jq("#message").html("");
    jq("#message").html(message);
    if (patientqueue.readMessageDialog == null) {
        patientqueue.createReadMessageDialog();
    }
    patientqueue.readMessageDialog.show();
};

patientqueue.closeReadMessageDialog = function () {
    patientqueue.readMessageDialog.close();
};


patientqueue.buildAlertAttributeParams = function () {
    var params = {};
    params['alert_message_id'] = patientqueue.alert_message_id;
    return params;
}


patientqueue.createReadMessageDialog = function () {
    patientqueue.readMessageDialog = emr.setupConfirmationDialog({
        selector: '#read_message',
        actions: {
            confirm: function () {
                emr.getFragmentActionWithCallback('patientqueueing', 'alerts', 'markAlertAsRead', patientqueue.buildAlertAttributeParams(),
                    function (v) {
                        jq('#read_message' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        emr.navigateTo({ applicationUrl: emr.applyContextModel("patientqueueing/clinicianDashboard.page")});
                    });
            },
            cancel: function () {
                patientqueue.readMessageDialog.close();
            }
        }
    });

    patientqueue.readMessageDialog.close();
}


patientqueue.showCreateMessageDialog = function (message,alert_message_id) {
    patientqueue.message = message;
    patientqueue.alert_message_id=alert_message_id;
    jq("#message").html("");
    jq("#message").html(message);
    if (patientqueue.createMessageDialog == null) {
        patientqueue.createReadMessageDialog();
    }
    patientqueue.createMessageDialog.show();
};

patientqueue.closeReadMessageDialog = function () {
    patientqueue.createMessageDialog.close();
};


patientqueue.buildAlertAttributeParams = function () {
    var params = {};
    params['alert_message_id'] = patientqueue.alert_message_id;
    return params;
}



patientqueue.buildVisitTypeAttributeParams = function (patientId, patientQueueId) {
    var params = {};
    params['patientId'] = patientqueue.patientId;
    params['patientQueueId'] = patientqueue.patientQueueId
    return params;
}