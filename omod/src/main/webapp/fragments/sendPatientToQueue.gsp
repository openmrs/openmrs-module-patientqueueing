<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script>
    if (jQuery) {
        jq(document).ready(function () {
            jq("#create").click(function () {
                jq.post('${ ui.actionLink("create") }', {
                    patientId: jq("#patient_id").val().trim(),
                    providerId: jq("#provider_id").val().trim(),
                    locationId: jq("#location_id").val().trim()
                }, function (response) {
                    var jsonToastMessage = JSON.parse(JSON.stringify(response).replace("toastMessage=", "\"toastMessage\":").trim());

                    jq().toastmessage('showSuccessToast', jsonToastMessage.toastMessage.message);
                    window.location.reload();
                });
            });
        });
    }
</script>
<style>
.modal-header {
    background: #000;
    color: #ffffff;
}
</style>

<div id="send_patient_to_queue_dialog" title="${ui.message("patientqueueing.task.sendPatientToQueue.label")}"
     class="dialog" style="display: none;">
    <div class="dialog-header">
        ${ui.message("patientqueueing.task.sendPatientToQueue.label")}
    </div>

    <div class="dialog-content">
        <span id="send_to_queue-container">

        </span>
        <input type="hidden" id="patient_id" value="">

        <div class="form-group">
            <label for="location_id">${ui.message("patientqueueing.location.label")}</label>
            <select class="form-control" id="location_id">
                <option value="">${ui.message("patientqueueing.location.selectTitle")}</option>
                <% if (locationList != null) {
                    locationList.each { %>
                <option value="${it.uuid}">${it.name}</option>
                <%
                        }
                    }
                %>
            </select>
            <span class="field-error" style="display: none;"></span>
            <% if (locationList == null) { %>
            <div><${ui.message("patientqueueing.select.error")}</div>
            <% } %>
        </div>

        <div class="form-group">
            <label for="provider_id">${ui.message("patientqueueing.provider.label")}</label>
            <select class="form-control" id="provider_id">
                <option value="">${ui.message("patientqueueing.provider.selectTitle")}</option>
                <% if (providerList != null) {
                    providerList.each {
                        if (it.getName() != null) { %><option
                    value="${it.providerId}">${it.getName()}</option><% }
            }
            } %>
            </select>
            <span class="field-error" style="display: none;"></span>
            <% if (locationList == null) { %>
            <div><${ui.message("patientqueueing.select.error")}</div>
            <% } %>
        </div>


        <div>
            <button class="cancel">${ui.message("coreapps.cancel")}</button>
            <button class="confirm" id="create">${ui.message("patientqueueing.send.label")}</button>
        </div>
    </div>
</div>
