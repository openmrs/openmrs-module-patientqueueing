<% if (clinicianLocationUUIDList?.contains(currentLocation?.uuid)) { %>
<%
        ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
%>
<script>

    if (jQuery) {
        jq(document).ready(function () {
            getPatientQueue();
            jq("#patient-search").change(function () {
                if (jq("#patient-search").val().length >= 3) {
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
            searchfilter: jq("#patient-search").val().trim().toLowerCase()
        }, function (response) {
            if (response) {
                var responseData = JSON.parse(response.replace("patientQueueList=", "\"patientQueueList\":").trim());
                displayData(responseData);
            } else if (!response) {
                jq("#clinician-queue-list-table").append(${ ui.message("coreapps.none ") });
            }
        });
    }

    function displayData(response) {
        var content = "";
        content = "<table><thead><tr><th>Queue ID</th><th>Names</th><th>Age (Years)</th><th>PREVIOUS LOCATION</th><th>PREVIOUS PROVIDER</th><th>WAITING TIME</th><th>ACTION</th></tr></thead><tbody>";
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
            content += "<i class=\"icon-exchange edit-action\" title=\"Transfer To Another Provider\" onclick='urlTransferPatientToAnotherQueue'></i>".replace("urlTransferPatientToAnotherQueue", urlTransferPatientToAnotherQueue);
            content += "<i class=\"icon-exchange edit-action\" data-toggle=\"modal\" data-target=\"#add_patient_to_queue_dialog\" data-id=\"\" data-whatever=\"@mdo\" data-order_id=\"%s\"></i>".replace("%s", patientQueueListElement.patientId);
            content += "<i class=\"icon-envelope view-action\" title=\"Transfer To Another Provider\" onclick='urlAlert'><span style=\"color: red\">1</span></i>".replace("urlAlert", urlAlert);
            content += "</td>";
            content += "</tr>";
        }
        content += "</tbody></table>";

        jq("#clinician-queue-list-table").append(content);
    }
</script>

<div class="info-section" id="clinician-list">
    <div class="info-header">
        <i class="icon-diagnosis"></i>

        <h3 style="width: 50%">${ui.message("patientqueueing.patientinqueue.title.label")}</h3> <span
            style="right:auto;width: 40%;font-weight: bold"></span>
    </div>
    <span>
        <form method="get" id="patient-search-form" onsubmit="return false">
            <input type="text" id="patient-search"
                   placeholder="${ui.message("coreapps.findPatient.search.placeholder")}" autocomplete="off"/><i
                id="patient-search-clear-button" class="small icon-remove-sign"></i>
        </form>
    </span>

    <div class="info-body">
        <div id="clinician-queue-list-table">
        </div>
    </div>
</div>
<% } %>