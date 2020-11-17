<%
    // although called "patientDashboard" this is actually the patient visits screen, and clinicianfacing/patient is the main patient dashboard
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.message("coreapps.app.systemAdministration.label")}",
            link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'
        },
        {label: "${ui.message("patientqueueing.room.label")}"}
    ];
</script>

<script>
    if (jQuery) {
        jq(document).ready(function () {
            jq('#addEditQueueRoomModel').on('show.bs.modal', function (event) {
                var button = jq(event.relatedTarget);
                var queueRoomId = button.data('queue_room_id');
                var modal = jq(this);

                modal.find("#queueRoomId").val("");
                modal.find("#queueRoomName").val("");
                modal.find("#location select").find().val("");
                modal.find("#description").val("");

                jq.get('${ ui.actionLink("patientqueue","queueRoom","getQueueRoom",) }', {
                    "queueRoomId": queueRoomId
                }, function (response) {
                    var queueRoom = JSON.parse(response.replace("queueRoom=", "\"queueRoom\":").trim());
                    modal.find("#queueRoomId").val(queueRoomId);
                    modal.find("#queueRoomName").val(queueRoom.queueRoom.name);
                    modal.find("#location select").find().val(queueRoom.queueRoom.location);
                    modal.find("#description").val(queueRoom.queueRoom.description);
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
                });
            });
        });
    }
</script>
<style>
.dashboard .que-container {
    display: inline;
    float: left;
    width: 65%;
    margin: 0 1.04167%;
}

.dashboard .alert-container {
    display: inline;
    float: left;
    width: 30%;
    margin: 0 1.04167%;
}

.dashboard .action-section ul {
    background: #63343c;
    color: white;
    padding: 7px;
}

.card-body {
    -ms-flex: 1 1 auto;
    flex: 7 1 auto;
    padding: 1.0rem;
    background-color: #eee;
}

.my-tab .tab-pane {
    border: solid 1px blue;
}

.vertical {
    border-left: 1px solid #c7c5c5;
    height: 79px;
    position: absolute;
    left: 99%;
    top: 11%;
}

#patient-search {
    min-width: 96%;
    color: #363463;
    display: block;
    padding: 5px 10px;
    height: 45px;
    margin-top: 27px;
    background-color: #FFF;
    border: 1px solid #dddddd;
}
</style>

<div class="card">
    <div class="card-header">
        <div class="">
            <div class="row">
                <div class="col-3">
                    <div>
                        <h2 style="color: maroon">${ui.message("patientqueueing.room.label")}</h2>
                    </div>

                    <div class="">

                        <button type="button" style="font-size: 25px" class="confirm icon-plus-sign" data-toggle="modal" data-target="#addEditQueueRoomModel" data-whatever="@mdo">Create</button>
                    </div>

                    <div class="vertical"></div>
                </div>

                <div class="col-8">
                    <form method="get" id="patient-search-form" onsubmit="return false">
                        <input type="text" id="patient-search"
                               placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                               autocomplete="off"/>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <div class="info-body">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>NAME</th>
                    <th>DATE</th>
                    <th>UUID</th>
                    <th>ACTION</th>
                </tr>
                </thead>
                <tbody>
                <%
                    queueRooms.each { %>
                <tr>
                    <td>${it.queueRoomId}</td>
                    <td>${it.name}</td>
                    <td>${it.dateCreated}</td>
                    <td>${it.uuid}</td>
                    <td>
                        <i style="font-size: 25px" data-toggle="modal" data-target="#addEditQueueRoomModel"
                           data-queue_room_id="${it.uuid}" class="icon-edit edit-action" title="Edit"></i>
                    </td>
                    <% } %>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>


<div class="modal fade" id="addEditQueueRoomModel" tabindex="-1" role="dialog"
     aria-labelledby="addEditQueueRoomModelLabel"
     aria-hidden="true">
    <div class="modal-dialog  modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="addEditQueueRoomModelLabel">${ui.message("patientqueueing.room.label")}</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>

            <form method="post">
                <input type="hidden" name="queueRoomId" id="queueRoomId" value="">
                <div class="modal-body">
                    <div class="container">
                        <div class="row">
                            <div class="col-6">
                                <div class="form-group">
                                    <label>${ui.message("patientqueueing.room.name")}</label>
                                    <input type="text" class="form-control" id="queueRoomName"
                                           placeholder=" ie Send Tests to Reference lab" name="queueRoomName">
                                </div>

                            </div>

                            <div class="col-6">
                                <div class="form-group">
                                    <label>${ui.message("patientqueueing.location.label")}</label>
                                    <select class="form-control" name="location" id="location">
                                        <option value="">Select Location</option>

                                        <%
                                            location.each { %>
                                        <option value="${it.locationId}">${it.name}</option>
                                        <% } %>
                                    </select>

                                </div>
                            </div>

                            <div class="row">
                                <div class="col-sm-6">

                                    <div class="form-group">
                                        <label>${ui.message("patientqueueing.room.description")}</label>
                                        <textarea class="form-control" id="description" placeholder="Description" name="description"  rows="5" cols="100"></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary"
                                data-dismiss="modal">${ui.message("patientqueueing.close.label")}</button>
                        <input type="submit" class="confirm" value="${ui.message("patientqueueing.save.label")}">
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

