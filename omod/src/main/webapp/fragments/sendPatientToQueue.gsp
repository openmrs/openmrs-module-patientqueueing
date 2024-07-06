<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script>
    if (jQuery) {

        function addQueueRoomOption(queueRooms, currentLocationUUID) {
            var queueRoomOptions = [];
            var sel = document.getElementById('queue_room_location');
            for (var i = 0 in queueRooms) {
                if (queueRooms[i].parentLocation.uuid === currentLocationUUID) {
                    var opt = document.createElement('option');
                    opt.appendChild( document.createTextNode(queueRooms[i].name) );
                    opt.value = queueRooms[i].uuid;
                    sel.appendChild(opt);
                    queueRoomOptions.push(queueRooms[i].uuid)
                }
            }

            if(queueRoomOptions.length>0){
                document.getElementById('patient_queue_room').style.display="block";
            }else {
                document.getElementById('patient_queue_room').style.display="none";
            }
        }

        jq(document).ready(function () {

            document.getElementById('patient_queue_room').style.display="none";

            jq("#location_id").change(function () {
                jq.ajax({
                    type: "GET",
                    url: '/' + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/location?v=full&tag=c0e1d1d8-c97d-4869-ba16-68d351d3d5f5",
                    dataType: "json",
                    async: false,
                    success: function (data) {
                        serverResponse = data.results;
                        addQueueRoomOption(serverResponse,  document.getElementById('location_id').options[document.getElementById('location_id').selectedIndex].value);
                    }
                });
            });

            jq("#create").click(function () {
                jq.post('${ ui.actionLink("create") }', {
                    patientId: jq("#patient_id").val().trim(),
                    providerId: jq("#provider_id").val().trim(),
                    locationId: jq("#location_id").val().trim(),
                    queueRoom: jq("#queue_room_location").val().trim()
                }, function (response) {
                    if (response.hasOwnProperty("toastMessage")) {
                        jq().toastmessage('showSuccessToast', response.toastMessage);
                    } else if (response.toString().includes("toastMessage=")) {
                        let jsonToastMessage = JSON.parse(response.replace("toastMessage=", "\"toastMessage\":").trim());
                        jq().toastmessage('showSuccessToast', jsonToastMessage.toastMessage);
                    }
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

        <div class="form-group" id="patient_queue_room">
            <label for="queue_room_location">${ui.message("patientqueueing.room.name")}</label>
            <select class="form-control" id="queue_room_location">
                <option value="">${ui.message("patientqueueing.room.select.name")}</option>
            </select>
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
