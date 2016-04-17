function scmFormSave(form) {
    Q(form).submit();
}

function scmFieldChanged(elem) {
    var jElem = Q(elem);
    var name = jElem.attr('name');
    var form = jElem.attr('_form');
    var action = jElem.attr('_action');
    var record = jElem.attr('_record');
    if ( form === "DPM_DATABASES" && name === "field_OPERATION" ) {
        DPM_dbApplyOperationChanged(elem);
    } else if ( form === "DPM_MESGQUEUE" && name === "field_OPERATION" ) {
        DPM_mqApplyOperationChanged(elem)
    } else if ( form === "DPM_MESGQUEUE" && name === "field_OBJECT" ) {
        DPM_mqApplyObjectChanged(elem)
    } else if ( form == "DPM_PARAMETERS" && name == "field_INPUTYPE" ) {
        DPM_paApplyInputTypeChanged(elem)
    }
}

function attachmentRevert(elem) {
    var sf = Q(elem).hide();
    var li = sf.closest("li");
    li.removeClass("bill99scm-deleted");
    li.find("a.bill99scm-attdelete").show();
    var ip = li.find("input[type=hidden]");
    var name = ip.attr("_name");
    ip.attr("name", name);
    ip.removeAttr("_name");
}

function attachmentDelete(elem) {
    var sf = Q(elem).hide();
    var li = sf.closest("li");
    li.addClass("bill99scm-deleted");
    li.find("a.bill99scm-attrevert").show();
    var ip = li.find("input[type=hidden]");
    var name = ip.attr("name");
    ip.attr("_name", name);
    ip.removeAttr("name");
}

//  special for current form
function DPM_mqApplyOperationChanged(element, skipSwithObjects) {

    var operation = Q(element).val();
    var form = Q(element).closest("form");
    var objectInput = form.find("select[name=field_OBJECT]");
    var object = objectInput.val();
    var objectItem = objectInput.closest("tr").find("td");
    
    if ( operation == "" ) {
        return;
    }
    if ( typeof(skipSwithObjects) !== "boolean" ) {
        skipSwithObjects = true;
    }
    
    if ( skipSwithObjects === true )  {
        if ( operation == "other" ) {
            objectItem.each( function (index) {
                if ( index > 1 ) {
                    Q(this).hide();
                }
            } );
        } else {
            var o = null;
            objectItem.show();
            objectInput.find("option").each( function () {
                var v = Q(this).val();
                Q(this).attr("disabled", "disabled").hide();
                if ( operation === "setprop" || operation === "addprop" ) {
                    if ( v == "queue" || v == "topic" || v == "route" || v == "factory" ) {
                        Q(this).removeAttr("disabled").show();
                        if ( o === null ) {
                            object = v;
                            this.selected = true;
                        }
                    }
                } else if ( operation === "create" || operation === "delete" ) {
                     if ( v == "queue" || v == "topic" || v == "route" || v == "factory"
                       || v == "jndiname" || v == "rvcmlistener" || v == "durable"
                       || v == "bridge" || v == "group" || v == "user" ) {
                        Q(this).removeAttr("disabled").show();
                        if ( o === null ) {
                            object = v;
                            this.selected = true;
                        }
                    }
                }
                if ( operation === "delete" ) {
                    if ( v == "connection" || v == "message" ) {
                        Q(this).removeAttr("disabled").show();
                        if ( o === null ) {
                            object = v;
                            this.selected = true;
                        }
                    }
                }
            } );
        }
    }
    if (operation == "" || (operation != "other" && object == "")) {
        return;
    }
    var rowSz = form.find("*[name=field_MQSIZE]")
                    .closest("tr");
    var rowExp = form.find("*[name=field_EXPIRED]")
                    .closest("tr");
    var rowPer = form.find("*[name=field_PERSISTENCE]")
                    .closest("tr");
    rowPer.hide(); rowSz.hide(); rowExp.hide();
    if ( operation !== "delete" && (object == "queue" ||　object == "topic") ) {
        rowPer.show(); rowSz.show(); rowExp.show();
    }
    var rowRtc = form.find("*[name=field_RETRYCOUNT]")
                    .closest("tr");
    rowRtc.hide();
    if ( operation !== "delete" && object == "queue" ) {
        rowRtc.show();
    }
}

function DPM_mqApplyObjectChanged(element) {
    var form = Q(element).closest("form");
    if ( form.find("select[name=field_OBJECT]").is(":hidden") ) {
        return;
    }
    DPM_mqApplyOperationChanged(form.find("select[name=field_OPERATION]"), false)
}

function DPM_dbApplyOperationChanged(elem) {
    var operation = Q(elem).val();
    var attachments = Q(elem).closest("form").find(
        "input[type=file][name~field_ATTACHMENTS]"
    ).closest("tr");
    if ( operation === "sqlldr" ) {
        attachments.show();
    } else {
        attachments.hide();
    }
}

function DPM_paApplyInputTypeChanged(elem) {
    var selected = Q(elem).val();
    var form = Q(elem).closest("form");
    form.find("div[field^=field_]").each( function() {
        var field = Q(this).attr("field")
        if ( selected == "import" ) {
            if (field == "field_INPUTYPE" || field == "field_APPLICATION" || field == "field_ATTACHMENTS") {
                Q(this).show();
            } else {
                Q(this).hide();
            }
           
        } else {
             if ( field == "field_APPLICATION" || field == "field_INPUTYPE" || field == "field_PARAMNAME"
               || field == "field_PARAMVALUE" || field == "field_PRODUCT" || field == "field_OPERATION"
               || field == "field_ENCRYPT" || field == "field_RESTART" ) {
                Q(this).show();
            } else {
                Q(this).hide();
            }
        }
    } );
}