<script>
    var patientQueueId="${patientQueueId}";
    if (jQuery) {
        jq(document).ready(function () {
            jq("input[id=patientQueueId").val(patientQueueId);
        });
    }
</script>

<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<div id="complete_patient_queue" title="${ui.message("patientqueueing.completePatientQueue.title.label")}"
     class="dialog"
     style="display: none;">
    <div class="dialog-header">
        ${ui.message("patientqueueing.completePatientQueue.title.label")}
    </div>

    <form method="post">
        <div class="dialog-content">
            <div class="modal-content">

                <div>
                    <p class="center" style="text-align: center;font-weight: bolder">
                        <input type="hidden" name="patientQueueId" id="patientQueueId" value="">
                        ${ui.message("patientqueueing.complete.dialogeMessage")}
                    </p>
                </div>

            </div>

            <div>
                <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
                <input type="submit" class="confirm" value="${ui.message("patientqueueing.complete.label")}">
            </div>
        </div>
    </form>
</div>

