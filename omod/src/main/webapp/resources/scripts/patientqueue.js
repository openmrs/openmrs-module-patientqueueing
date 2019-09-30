var patientqueue = patientqueue || {};

patientqueue.completePatientQueueDialog = null;
patientqueue.patientId = null;
patientqueue.sendPatientQueueDialog = null;
patientqueue.readMessageDialog = null;
patientqueue.alert_message_id = null;
patientqueue.message = null;
patientqueue.createMessageDialog = null;


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
};


patientqueue.createReadMessageDialog = function () {
    patientqueue.readMessageDialog = emr.setupConfirmationDialog({
        selector: '#read_message',
        actions: {
            confirm: function () {
                emr.getFragmentActionWithCallback('patientqueueing', 'alerts', 'markAlertAsRead', patientqueue.buildAlertAttributeParams(),
                    function (v) {
                        jq('#read_message' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        emr.navigateTo({ applicationUrl: emr.applyContextModel("patientqueueing/providerDashboard.page")});
                    });
            },
            cancel: function () {
                patientqueue.readMessageDialog.close();
            }
        }
    });
    patientqueue.readMessageDialog.close();
}