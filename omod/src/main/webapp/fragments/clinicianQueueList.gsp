<% if (clinicianLocationUUIDList?.contains(currentLocation?.uuid)) { %>
<%
        ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
%>
<script>

    if (jQuery) {

        function addQueueRoomsToSelect(queueRooms, currentLocationUUID) {
            var queueRoomOptions = "";
            var sel = document.getElementById('queue-room-location');
            for (var i = 0 in queueRooms) {
                if (queueRooms[i].parentLocation.uuid === currentLocationUUID) {
                    var opt = document.createElement('option');
                    opt.appendChild( document.createTextNode(queueRooms[i].name) );
                    opt.value = queueRooms[i].uuid;
                    sel.appendChild(opt);
                }
            }
        }

        jq(document).ready(function () {
            jq.ajax({
                type: "GET",
                url: '/' + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/location?v=full&tag=c0e1d1d8-c97d-4869-ba16-68d351d3d5f5",
                dataType: "json",
                async: false,
                success: function (data) {
                    serverResponse = data.results;
                    addQueueRoomsToSelect(serverResponse, "${currentLocation.uuid}");
                }
            });


            getPatientQueue();
            jq("#patient-search").change(function () {
                if (jq("#patient-search").val().length >= 3) {
                    getPatientQueue();
                }
            });

            jq("#queue-room-location").change(function () {
                if (jq("#queue-room-location").val().length >= 3) {
                    getPatientQueue();
                }
            });

            jq('#exampleModal').on('show.bs.modal', function (event) {
                var button = jq(event.relatedTarget) // Button that triggered the modal
                var recipient = button.data('whatever') // Extract info from data-* attributes
                var order_id = button.data('order_id') // Extract info from data-* attributes
                jq("#order_id").val(order_id);
                jq("#sample_id").val("");
                jq("#reference_lab").prop('selectedIndex', 0);
                jq("#specimen_source_id").prop('selectedIndex', 0);
                jq("#refer_test input[type=checkbox]").prop('checked', false);
                // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
                // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
                var modal = jq(this)
                modal.find('.modal-title').text('New message to ' + order_id)
                modal.find('.modal-body input').val(order_id)
            })
        });
    }

    function getWaitingTime(queueDate) {
        var diff = Math.abs(new Date() - new Date(queueDate));
        var seconds = Math.floor(diff / 1000); //ignore any left over units smaller than a second
        var minutes = Math.floor(seconds / 60);
        seconds = seconds % 60;
        var hours = Math.floor(minutes / 60);
        minutes = minutes % 60;
        return hours + ":" + minutes + ":" + seconds
    }

    function getPatientQueue() {
        jq("#clinician-queue-list-table").html("");
        jq.get('${ ui.actionLink("getPatientQueueList") }', {
            searchfilter: jq("#patient-search").val().trim().toLowerCase(),
            queueRoom: document.getElementById("queue-room-location").options[document.getElementById("queue-room-location").selectedIndex].value
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(String(response).replace("patientQueueList=", "\"patientQueueList\":").trim());
                displayData(responseData);
            } else if (!response) {
                jq("#clinician-queue-list-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    function displayData(response) {
        var content = "";
        var stillInQueue = 0;
        content = "<table><thead><tr><th>NO. IN QUEUE</th><th>Names</th><th>Age (Years)</th><th>PREVIOUS LOCATION</th><th>ASSIGNED PROVIDER</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
        for (i = 0; i < response.patientQueueList.length; i++) {
            var patientQueueListElement = response.patientQueueList[i];
            var urlToPatientDashBoard = '${ui.pageLink("coreapps","clinicianfacing/patient",[patientId: "patientIdElement"])}'.replace("patientIdElement", patientQueueListElement.patientId);
            var urlTransferPatientToAnotherQueue = 'patientqueue.showAddPatientQueueDialog("patientIdElement")'.replace("patientIdElement", patientQueueListElement.patientId);
            var urlAlert = 'patientqueue.showReadMessageDialog("text", "alertId")';
            var waitingTime = getWaitingTime(patientQueueListElement.dateCreated);
            content += "<tr>";
            content += "<td>" + patientQueueListElement.patientQueueId + "</td>";
            content += "<td>" + patientQueueListElement.patientNames + "</td>";
            content += "<td>" + patientQueueListElement.age + "</td>";
            content += "<td>" + patientQueueListElement.locationFrom + "</td>";
            content += "<td>" + patientQueueListElement.providerNames + "</td>";
            content += "<td>" + waitingTime + "</td>";
            content += "<td>";
            content += "<i class=\"icon-dashboard view-action\" title=\"Goto Patient's Dashboard\" onclick=\"location.href = 'urlToPatientDashboard'\"></i>".replace("urlToPatientDashboard", urlToPatientDashBoard);
            content += "</td>";
            content += "</tr>";
            stillInQueue += 1;
        }
        content += "</tbody></table>";
        jq("#clinician-pending-number").html("");
        jq("#clinician-pending-number").append("   " + stillInQueue);
        jq("#clinician-queue-list-table").append(content);

    }
</script>

<div class="card">
    <div class="card-header">
        <div class="">
            <div class="row">
                <div class="col-3">
                    <div>
                        <h3 style="color: maroon">${currentLocation.name}</i></h3>
                    </div>

                    <div style="text-align: center">
                        <h4>${currentProvider?.personName?.fullName}</h4>
                    </div>

                    <div class="vertical"></div>
                </div>


                    <form method="get" id="patient-search-form" onsubmit="return false">
                        <div class="col-4">
                        <input type="text" id="patient-search"
                               placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                               autocomplete="off"/><i
                            id="patient-search-clear-button" class="small icon-remove-sign"></i>
                        </div>
                        <div class="col-2">
                            <select name="queueRoom" id="queue-room-location">
                                <option id="">Select Queue Room</option>
                        </select>
                        </div>
                    </form>
            </div>
        </div>
    </div>

    <div class="card-body">
        <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
            <li class="nav-item">
                <a class="nav-item nav-link active" id="home-tab" data-toggle="tab" href="#clinician-pending" role="tab"
                   aria-controls="clinician-pending-tab" aria-selected="true">${
                        ui.message ( "patientqueueing.clinicianQueueList.numberInQueue" )} <span style="color:red"
                                                                                                 id="clinician-pending-number">0</span>
                </a>
            </li>
        </ul>

        <div class="tab-content" id="myTabContent">
            <div class="tab-pane fade show active" id="clinician-pending" role="tabpanel"
                 aria-labelledby="clinician-pending-tab">
                <div class="info-body">
                    <div id="clinician-queue-list-table">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<% } %>