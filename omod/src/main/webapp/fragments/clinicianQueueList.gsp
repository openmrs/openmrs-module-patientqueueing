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
            content += "<i class=\"icon-envelope view-action\" title=\"Transfer To Another Provider\" onclick='urlAlert'><span style=\"color: red\">1</span></i>".replace("urlAlert", urlAlert);
            content += "</td>";
            content += "</tr>";
        }
        content += "</tbody></table>";

        jq("#clinician-queue-list-table").append(content);
    }
</script>

<div class="info-section">
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
