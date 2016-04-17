package org.socyno.jenkins.formsys.Record

import org.socyno.jenkins.formsys.Messages
import org.socyno.jenkins.formsys.Utils
import org.socyno.jenkins.formsys.Users
import org.socyno.jenkins.formsys.Record
import org.socyno.jenkins.formsys.Actions

gAction = my.getBindAction();
gIsEdit = gAction.getName() != Actions.ACTION_VIEW

gPageTile = ( my.isNew() ? Messages.SCMFormRecordNew() : (
        gIsEdit ? Messages.SCMFormRecordEdit() : Messages.SCMFormRecordView()
    )  )  + " : " + my.form.getDisplay() + (
        my.isNew() ? "" : "(" + sprintf("%08d", my.getId()) + ")"
    )

def drawFieldName(field) {
    if ( field.required && gIsEdit )  {
        font(class:"bill99scm-required") { text("* ") }
    }
    text(_(field.getDisplay()))
}

def drawFieldHelpText(field) {
    pre(class:"bill99scm-block bill99scm-fieldhelp") {
        text(field.helpText)
    }
}

def drawInputTextArea(name, value="", readonly=false) {
    def attrs = [
        rows : 4,
        name : name,
        class : "bill99scm-width100 bill99scm-textarea"
    ]
    if ( !gIsEdit || readonly ) {
        attrs["readonly"] = "readonly"
        attrs["class"] += " bill99scm-readonly"
    }
    textarea(attrs) { text(value) }
}

def drawInputTextField(name, value=" ", readonly=false, filled=true) {
    def attrs = [
        name : name,
        type : "text",
        value : value,
        class : "bill99scm-textfield"
    ]
    if ( filled ) {
        attrs["class"] += " bill99scm-width100"
    }
    if ( !gIsEdit || readonly ) {
        attrs["readonly"] = "readonly"
        attrs["class"] += " bill99scm-readonly"
    }
    input(attrs)
}

def drawInputSelect(name, options, value="", disabled=false, filled=true) {
    def attrs = [
        name : name,
        _form : my.form.getName(),
        _action : gAction.getName(),
        _record : my.getId(),
        onchange : "scmFieldChanged(this)",
        class : "bill99scm-select"
    ]
    if ( filled ) {
        attrs["class"] += " bill99scm-width100"
    }
    if ( !gIsEdit || disabled ) {
        attrs["disabled"] = "disalbed"
        attrs["class"] += " bill99scm-readonly"
    }
    if ( value instanceof Users.Item
      || value instanceof Record ) {
        value = value.getId();
    }
    select (attrs) {
        def vFond = false;
        options.values().each() { o ->
            def ops = [value : o.getKey()]
            if ( Utils.nullAsEmpty(value) == Utils.nullAsEmpty(o.getKey()) ) {
                vFond = true;
                ops["selected"] = "selected";
            }
            option(ops) { text(o.getValue()) }
        }
        if ( !vFond ) {
            option(value : value, selected : "selected") { text(value) }
        }
    }
}

def drawInputAddBox(name, vals=[], readonly=false) {
    attrs = [
        size : 5,
        multiple : "multiple",
        class : "bill99scm-block bill99scm-width100"
    ]
    if ( readonly ) {
        attrs["disabled"] = "disabled"
    }
    select( attrs ) {
        vals.each() { v ->
            if ( v == null ) {
                return;
            }
            option(value : v) {
                text(v)
                input (type : "hidden", name : name + "[]", value : v)
            }
        }
    }
    attrs = [
        class : "bill99scm-block"
    ]
    if ( readonly ) {
        attrs["class"] = "bill99scm-hidden"
    }
    div( attrs ) {
        input (
            type : "button",
            title : Messages.SCMRecordFieldValueAddInfo(),
            class : "delete bill99scm-smallBT",
            value : Messages.SCMRecordFieldValueAdd(),
            onclick : "scmRecordFieldValueAdd(this)"
        )
        input (
            type : "button",
            title : Messages.SCMRecordFieldValueDeleteInfo(),
            class : "delete bill99scm-smallBT",
            value : Messages.SCMRecordFieldValueDelete(),
            onclick : "scmRecordFieldValueDelete(this)"
        )
    }
    input (type : "hidden", name : name, value : 1);
}

def drawInputCheckBox () {

}

def drawInputAttachments(name, options, allowMulti=true, readonly=false) {
    if ( options instanceof String ) {
        options = [options]
    } else if ( !(options instanceof String[]) ) {
        options = [];
    }
    if ( !gIsEdit ) {
        readonly = true;
    }
    ul( class : "bill99scm-attachments bill99scm-width100" ) {
        options.each() { o ->
            li( class : "bill99scm-attachment bill99scm-width100" ) {
                font() {text(o)}
                a(class:"bill99scm-attdelete" + readonly ? " bill99scm-hidden" : "" , href:"javascript:void(0)", onclick:"attachmentDelete(this)"){
                    text(Messages.CommonDelete())
                }
                a(class:"bill99scm-attrevert bill99scm-hidden", href:"javascript:void(0)", onclick:"attachmentRevert(this)"){
                    text(Messages.CommonRevert())
                }
                a(class:"bill99scm-attdownload", href: rootURL + "/bill99scm/attach?id=" + o){
                    text(Messages.CommonDownload())
                }
                input (type : "hidden", name : name + "[]", value : o);
            }
        }
    }
    if ( !readonly ) {
        input (
            type : "file",
            name : name + "[]",
            class:"bill99scm-width100 bill99scm-inputfile bill99scm-block",
        )
        if ( allowMulti ) {
            input (
                onclick:'''
                    var t = Q(this).closest("div").find("input[type=file]:last");
                    t.clone().insertAfter(t);
                ''',
                type : "button", 
                class : "bill99scm-samllBT",
                value :_(Messages.CommonFormAdd())
            )
            input (
                onclick:'''
                    var f = Q(this).closest("div").find("input[type=file]");
                    if ( f.size() > 1 ) { f.filter(":last").remove() }
                ''',
                type: "button",
                class:"bill99scm-samllBT",
                value:_(Messages.CommonFormDelete())
            )
        }
        if ( !readonly ) {
            input (type : "hidden", name : name, value : 1);
        }
    }
}

def drawFieldInput(field, filled=true) {
    def fVal = my.getFieldValue(field)
    
    // for ID
    if ( field.isId() ) {
        drawInputTextField(
            "",
            Utils.nullAsEmpty(my.isNew() ? "" : my.getId()),
            true,
            true
        )
        return;
    }
    
    // for long text
    if ( field.type.rawIsLongText() && field.type.rawIsArray() ) {
        // TODO
        return;
    }
    if ( field.type.rawIsLongText()  ) {
        drawInputTextArea(
            "field_" + field.name,
            Utils.nullAsEmpty(fVal),
            field.readonly
        )
        return;
    }
    
    // for attachments
    if ( field.type.rawIsFile() ) {
        drawInputAttachments(
            "field_" + field.name,
            fVal,
            field.type.rawIsArray(),
            field.readonly
        )
        return
    }
    // for reference
    if ( field.referred ) {
        if ( field.useSelectInput ) {
            drawInputSelect(
                "field_" + field.name,
                field.getOptions(),
                fVal,
                field.readonly,
                filled
            )
        } else {
            // TODO
        }
        return;
    }
    
    // for date time 
    if ( field.type.name() == "DATETIME" ) {
        drawInputTextField(
            "field_" + field.name,
            field.type.display(fVal),
            field.readonly,
            filled
        )
        return
    }
    
    if ( field.type.rawIsArray() ) {
        if ( field.useSelectInput ) {
            drawInputCheckbox (
                "field_" + field.name,
                field.getOptions(),
                fVal,
                field.readonly
            )
        } else {
            drawInputAddBox (
                "field_" + field.name,
                fVal,
                field.readonly
            )
        }
        return;
    }
    
    if ( field.useSelectInput ) {
        drawInputSelect(
            "field_" + field.name,
            field.getOptions(),
            Utils.nullAsEmpty(fVal),
            field.readonly,
            filled
        )
        return
    }
    
    // for default
    drawInputTextField(
        "field_" + field.name,
        Utils.nullAsEmpty((Object)fVal),
        field.readonly,
        filled
    )
}

gLayout = namespace(lib.LayoutTagLib)

gLayout.layout() {
    gLayout.header(title: _(gPageTile)) {
        script( 
            type : "text/javascript",
            src : resURL + "/plugin/bill99-scm/plugin.js"
        )
        script(
            type : "text/javascript",
            src : resURL + "/plugin/bill99-scm/record.js"
        )
        link(
            rel : "stylesheet",
            type : "text/css",
            href : resURL + "/plugin/bill99-scm/plugin.css"
        )
    }
    gLayout.side_panel() {
        if ( !gIsEdit ) {
             my.form.getActions().toArray().each() { a ->
                def n = a.getName()
                if ( n != Actions.ACTION_NEW
                  && n != Actions.ACTION_VIEW
                  && a.isAllowed(my) ) {
                    gLayout.task(
                        icon: "images/24x24/new-package.png",
                        href: "?action=" + a.getName(),
                        title: a.getDisplay()
                    )
                }
            }
        }              
    }
    gLayout.main_panel() {
        form(action: gIsEdit ? "save" : "record", method: gIsEdit ? "POST" : "GET") {
            if ( !my.isNew() ) {
                input( type : "hidden", name : "record", value :  my.getId() )
            }
            input( type : "hidden", name : "form", value : my.form.getName() )
            input( type : "hidden", name : "action", value : gAction.getName() )
            table(class:"bill99scm-form bill99scm-width80", cellspacing:"0", cellpadding:"0") {
                tr(class:"bill99scm-form-header") {
                    th(colspan:"4", class:"bill99scm-form-itemH") {
                        text(gPageTile)
                    }
                }
                def fields = my.form.getFields();
                for( i = 0; i < fields.size(); i++ ) {
                    def field = fields.get(i++);
                    def fieldNext = i == fields.size() ? null : fields.get(i);
                    if ( (fieldNext != null) && !field.displayBlocked && !fieldNext.displayBlocked ) {
                        tr(class:"bill99scm-form-row") {
                            [field, fieldNext].each() { f ->
                                attr = [
                                    class:"bill99scm-form-itemC"
                                ]
                                divAttr = [
                                    class : "bill99scm-block",
                                    field : "field_" + f.name
                                ]
                                if ( f.hidden ) {
                                    divAttr["class"] = "bill99scm-hidden"
                                }
                                if ( f.showTitle ) {
                                    td(class:"bill99scm-form-itemN") {
                                        div(divAttr) {
                                            drawFieldName(f)
                                        }
                                    }
                                } else {
                                    attr["colspan"] = 2
                                }
                                td( attr ) {
                                    div(divAttr) {
                                        drawFieldInput(f, f.showTitle)
                                        drawFieldHelpText(f)
                                    }
                                }
                            }
                        }
                    } else if ( field.displayBlocked || (fieldNext == null) || fieldNext.displayBlocked ) {
                        tr(class:"bill99scm-form-row") {
                            attr = [
                                class:"bill99scm-form-itemC"
                            ]
                            divAttr = [
                                class : "bill99scm-block",
                                field : "field_" + field.name
                            ]
                            if ( field.hidden ) {
                                divAttr["class"] = "bill99scm-hidden"
                            }
                            colspan = 1;
                            if ( field.displayBlocked ) {
                                colspan += 2
                            } 
                            if ( field.showTitle ) {
                                td(class:"bill99scm-form-itemN") {
                                    div(divAttr) {
                                        drawFieldName(field)
                                    }
                                }
                            } else {
                                colspan += 1
                            }
                            if ( colspan > 1 ) {
                                attr["colspan"] = colspan;
                            } 
                            td(attr) {
                                div(divAttr) {
                                    drawFieldInput(field, field.showTitle)
                                    drawFieldHelpText(field)
                                }
                            }
                            if ( !field.displayBlocked ) {
                                td(class:"bill99scm-form-itemN") {
                                    text(" ")
                                }
                                td(class:"bill99scm-form-itemC") {
                                    text(" ")
                                }
                            }
                        }
                        if ( fieldNext != null ) {
                            i--
                        }
                    } else if ( fieldNext != null ) {
                        i--
                    }
                }
                tr(class:"bill99scm-form-buttons") {
                    td(class:"bill99scm-form-itemB", colspan:"4") {
                        if ( gIsEdit ) {
                            input (
                                type : "button",
                                class : "bill99scm-button",
                                value : _(Messages.CommonFormSave()),
                                onclick : '''scmFormSave(Q(this).closest("form"))'''
                            )
                        }
                    }
                }
            }
        }
    }
}


