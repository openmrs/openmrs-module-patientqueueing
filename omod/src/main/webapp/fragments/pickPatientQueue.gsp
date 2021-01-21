<script>
    var patientQueueId = "${patientQueueId}";
    var provider = "${currentProvider.uuid}";
    var serverResponse;
    if (jQuery) {
        jq(document).ready(function () {
            jq("input[id=patientQueueId").val(patientQueueId);

            jq.ajax({
                type: "GET",
                url: '/' + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/location?v=full&tag=c0e1d1d8-c97d-4869-ba16-68d351d3d5f5",
                dataType: "json",
                async: false,
                success: function (data) {
                    serverResponse = data.results;
                    addQueueRoomOption(serverResponse, ${currentLocationUUID});
                }
            });


        });
    }


    function addQueueRoomOption(queueRooms, currentLocationUUID) {
        var queueRoomOptions = [];
        var sel = document.getElementById('queue_room_location');
        for (var i = 0 in queueRooms) {
            if (queueRooms[i].parentLocation.uuid === currentLocationUUID) {
                var opt = document.createElement('option');
                opt.appendChild(document.createTextNode(queueRooms[i].name));
                opt.value = queueRooms[i].uuid;
                sel.appendChild(opt);
                queueRoomOptions.push(queueRooms[i].uuid)
            }
        }

        if (queueRoomOptions.length > 0) {
            document.getElementById('patient_queue_room').style.display = "block";
        } else {
            document.getElementById('patient_queue_room').style.display = "none";
        }
    }
</script>

<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<div id="complete_patient_queue" title="${ui.message("patientqueueing.completePatientQueue.title.label")}"
     class="dialog"
     style="display: none;">
    <div class="dialog-header">
        ${ui.message("Pick Patient From Queue")}
    </div>

    <form method="post">
        <div class="dialog-content">
            <div class="modal-content">

                <div>
                    <p class="center" style="text-align: center;font-weight: bolder">
                        <input type="hidden" name="patientQueueId" id="patientQueueId" value="">
                    </p>
                </div>

                <div>
                    <p class="center" style="text-align: center;font-weight: bolder">
                        <input type="hidden" name="provider" id="provider" value="">
                    </p>
                </div>

                <div class="form-group" id="patient_queue_room">
                    <label for="queue_room_location">${ui.message("patientqueueing.room.name")}</label>
                    <select class="form-control" id="queue_room_location">
                        <option value="">${ui.message("patientqueueing.room.select.name")}</option>
                    </select>
                </div>

            </div>

            <div>
                <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
                <input type="submit" onsubmit="" class="confirm" value="${ui.message("patientqueueing.complete.label")}">
            </div>
        </div>
    </form>
</div>

