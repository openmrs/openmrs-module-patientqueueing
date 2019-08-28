<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script type="text/javascript">
    jq(document).ready(function () {
    });
</script>

<div id="complete_patient_queue" class="dialog" style="display: none">
    <form method="get" id="patient-search-form" onsubmit="return false">
        <div class="dialog-header">
            <i class="icon-remove-sign"></i>

            <h3>${ui.message("patientqueueing.completequeue.title.label")}</h3>
        </div>


        <div>
            <p class="center" style="text-align: center;font-weight: bolder">
                <input type="hidden" name="patientQueueId" value="">
                Are you sure you want to complete Patient Session.
            </p>
        </div>

        <div class="dialog-content form">
            <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
            <input type="submit" class="confirm" value="${ui.message("patientqueueing.ok.label")}">
        </div>
    </form>
</div>

