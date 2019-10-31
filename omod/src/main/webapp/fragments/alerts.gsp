<%
    ui.includeJavascript("patientqueueing", "patientqueue.js")
%>
<script>
    if (jQuery) {
        jq(document).ready(function () {
            jq("#okay").click(function () {
                jq.get('${ ui.actionLink("markAlertAsRead") }', {
                    alertMessageId: jq("#message_id").find("input[type=hidden]").val().trim().toLowerCase()
                }, function (response) {
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
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

<div class="action-section">
    <h3 style="text-align: center; color: black"><span
            style="color: red;">${alerts.size()}</span> ${ui.message("patientqueueing.alertmessages.title.label")}</h3>
    <ul style="max-height: 200px; overflow: scroll; width:100%;">
        <% alerts.each { %>
        <li><a onclick='patientqueue.showReadMessageDialog("${it.text}", "${it.alertId}")'><i
                class="icon-comment"></i>${it.text.take(40)}</a></li>
        <% } %>
    </ul>
</div>

<div class="modal fade" id="read_message" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                Message From
                <i class="icon-remove-sign"></i>
            </div>

            <div class="modal-body">
                <p id="message" class="center" style="text-align: center;font-weight: bolder">
                    Message Here
                </p>
                <input type="hidden" id="message_id" name="message_id" value="">
            </div>

            <div class="modal-footer form">
                <div class="dialog-content form">
                    <button class="cancel" id="">${ui.message("patientqueueing.close.label")}</button> <button
                        class="confirm" id="okay">${ui.message("patientqueueing.ok.label")}</button>
                </div>
            </div>

        </div>
    </div>
</div>

<div id="create_message" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-remove-sign"></i>

        <h3>Compose Message</h3>
    </div>

    <div>
        pan>
        <p>
            <span>
                <label for="provider_new_message">
                    <span>${ui.message("patientqueueing.provider.label")}</span>
                </label>
                <select name="providerId" id="provider_new_message">
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
                <% if (providerList == null) { %>
                <div><${ui.message("patientqueueing.select.error")}</div>
                <% } %>
            </span>
        </p>

        <p>
            <textarea name="message" id="message_to"></textarea>
        </p>

        <div class="dialog-content form">
            <button class="cancel" id="">${ui.message("patientqueueing.cancel.label")}</button> <button
                class="confirm" id="okay">${ui.message("patientqueueing.send.label")}</button>
        </div>
    </div>
</div>