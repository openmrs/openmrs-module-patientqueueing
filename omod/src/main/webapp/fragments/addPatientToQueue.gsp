<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>

<script type="text/javascript">
    jq(document).ready(function () {
    });
</script>

<div id="add_patient_to_queue_dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-remove-sign"></i>

        <h3>${ui.message("patientqueueing.task.addPatientToQueue.label")}</h3>
    </div>

    <div>
        <form method="post">
            <fieldset style="min-width: 40%">
                <span id="add_to_queue-container">

                </span>
                <input type="hidden" id="patient_id" name="patientId" value="">
                <p>
                    <span id="location-container">
                        <label for="location">
                            <span>${ui.message("patientqueueing.location.label")}</span>
                        </label>
                        <select name="locationId" id="location">
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
                    </span>
                </p>

            </span>
                <p>
                    <span id="provider-container">
                        <label for="provider">
                            <span>${ui.message("patientqueueing.provider.label")}</span>
                        </label>
                        <select name="providerId" id="provider">
                            <option value="">${ui.message("patientqueueing.provider.selectTitle")}</option>
                            <% if (providerList != null) {
                                providerList.each {
                                    if (it.getName() != null) {
                            %>

                            <option value="${it.providerId}">${it.getName()}</option>
                            <% }
                            }
                            }
                            %>
                        </select>
                        <span class="field-error" style="display: none;"></span>
                        <% if (locationList == null) { %>
                        <div><${ui.message("patientqueueing.select.error")}</div>
                        <% } %>
                    </span>
                </p>


                    <span>

                    </span>
                <div class="dialog-content form">
                    <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button>
                    <input type="submit" class="confirm" value="${ui.message("patientqueueing.send.label")}">
                </div>
            </fieldset>
        </form>
    </div>

</div>

