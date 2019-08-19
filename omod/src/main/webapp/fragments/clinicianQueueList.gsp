<div class="info-section">
    <div class="info-header">
        <i class="icon-diagnosis"></i>

        <h3 style="width: 50%">${ui.message("patientqueueing.patientinqueue.title.label")}</h3> <span style="right:auto;width: 40%;font-weight: bold"></span>
    </div>
    <span>
        <form method="get" id="patient-search-form" onsubmit="return false">
            <input type="text" id="patient-search"
                   placeholder="${ui.message("coreapps.findPatient.search.placeholder")}" autocomplete="off"
                   style="width: 10%;"/><i id="patient-search-clear-button" class="small icon-remove-sign"></i>
        </form>
    </span>

    <div class="info-body">
        <table>
            <thead>
            <tr>
                <th>Queue ID</th>
                <th>Names</th>
                <th>Age (Years)</th>
                <th>PREVIOUS LOCATION</th>
                <th>PREVIOUS PROVIDER</th>
                <th>WAITING TIME</th>
                <th>ACTION</th>
            </tr>
            </thead>
            <tbody>
            <% if (patientQueueList != null) {
                patientQueueList.each {
            %>
            <tr>
                <td>${it.patient.id}</td>
                <td>${it.patient.familyName} ${it.patient.givenName}</td>
                <td>${it.patient.age}</td>
                <td>${it.locationFrom.name}</td>
                <td></td>
                <td>${it.provider.name}</td>
                <td>
                    <i class="icon-dashboard view-action" title="Goto Patient's Dashboard" onclick="location.href = '${ui.pageLink("coreapps",'clinicianfacing/patient',[patientId: it.patient.patientId])}'"></i>
                    <i class="icon-exchange edit-action" title="Transfer To Another Provider" onclick='patientqueue.showAddPatientQueueDialog("${it.patient.patientId}")'></i>
                    <i class="icon-envelope view-action" title="Patient Note" onclick="location.href = '${ui.pageLink("patientqueueing",'addPatientToQueue',[patientId: it.patient.patientId])}'"><span style="color: red">1</span></i>
                </td>
            </tr>
            <% }
            } %>
            </tbody>
        </table>
    </div>
</div>
