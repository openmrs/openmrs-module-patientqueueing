<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script type="text/javascript">
    jq(document).ready(function () {
    });
</script>

<div id="complete_patient_queue" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-remove-sign"></i>

        <h3>${ui.message("patientqueueing.completequeue.title.label")}</h3>
    </div>

    <div>
        <p class="center" style="text-align: center;font-weight: bolder">
            Are you sure you want to complete Patient Session.
        </p>
    </div>

    <div class="dialog-content form">
        <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
        <button class="confirm right" id="submit">${ui.message("patientqueueing.ok.label")}<i class="icon-spinner icon-spin icon-2x"
                                                        style="display: none; margin-left: 10px;"></i></button>
    </div>
</div>

