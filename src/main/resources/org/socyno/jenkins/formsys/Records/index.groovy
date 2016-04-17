package org.socyno.jenkins.formsys.Records

import org.socyno.jenkins.formsys.Messages
import org.socyno.jenkins.formsys.Utils
import org.socyno.jenkins.formsys.SearchOrder


gLayout = namespace(lib.LayoutTagLib)
gFields = my.form.fields.getExtended(true);
gPageTile = Messages.SCMFormRecordList() + " : " + my.form.getDisplay()

def drawPager( pager, id, length=11 ) {
    def pagers = []
    if ( length < 1 ) {
        length = 1
    }
    def selected = pager.pageNumber
    def started = selected - (long)(length / 2)
    if ( started < 1  ) {
        started = 1
    }
    def stopped = started + length - 1
    if ( stopped > pager.pageCount ) {
        if ( started > 1 ) {
            started -= (stopped - pager.pageCount);
            if ( started < 1  ) { started = 1 }
        }
        stopped = pager.pageCount
    }
    
    ul(class : "bill99scm-pager") {
        def attrs = [
            class : "normal",
            onclick : "scmPager" + id + "(this)",
            onmouseout : "Q(this).removeClass('mouseover')",
            onmouseover : "Q(this).addClass('mouseover')"
        ]
        if ( started > 1 ) {
            li(attrs) {text(1)}
            if ( started > 2 ) {
                attrs.class = "normal omitted"; 
                li(attrs) {text("...")}
            }
        }
        while ( started <= stopped ) {
            attrs.class = started == selected ? "selected" : "normal"
            li(attrs) {text(started)}
            started++;
        }
        if ( stopped < pager.pageCount ) {
            if ( stopped + 1 < pager.pageCount ) {
                attrs.class = "normal omitted"; 
                li(attrs) {text("...")}
            }
            attrs.class = "normal";
            li(attrs) {text(pager.pageCount)}
        }
    }
}

gLayout.layout() {
    gLayout.header(title: _(gPageTile)) {
        script(type : "text/javascript", src : resURL + "/plugin/bill99-scm/json2.js")
        script(type : "text/javascript", src : resURL + "/plugin/bill99-scm/plugin.js")
        link(rel : "stylesheet", type : "text/css", href : resURL + "/plugin/bill99-scm/plugin.css")
    }
    gLayout.side_panel() {
        gLayout.task(
            icon: "images/24x24/new-package.png",
            href: "0",
            title: Messages.SCMActionDefaultNewDisplay()
        )
    }
    gLayout.main_panel() {
        script(type : "text/javascript", src : resURL + "/plugin/bill99-scm/records.js")
        table(class:"bill99scm-table bill99scm-width96 bill99scm-rsf-form", cellspacing:"0", cellpadding:"0") {
            tr(class : "display row") {
                td (class : "title") {
                   text(Messages.SCMSearchFieldsDisplayed())
                }
                td (class : "fields", colspan : 3) {
                    
                }
            }
            tr(class : "search row") {
                td (class : "title") {
                    text(Messages.SCMRecordsSearchFieldSelection())
                }
                td (class : "select", colspan : "3") {
                    select(class : "filter") {
                        option( value : " " ) {
                            text(Messages.SCMCommonSelectPlease())
                        }
                        gFields.each() { o ->
                            if ( o.getTarget().referred != null ) {
                                return;
                            }
                            ops = [];
                            o.getType().allowedOperators().each() { p ->
                                ops.add(p.toString() + "|" + (
                                    p.valueAllowed() ? "Y" : "N"
                                   ) + "|" + p.display());
                            }
                            option( value : o.getName() + "," + ops.join(",") ) {
                                text(o.getDisplay())
                            }
                        }
                    }
                    input (
                        type : "button",
                        class : "bill99scm-smallBT forFilter",
                        value : Messages.SCMRecordsSearchAddFilter(),
                        onclick : "scmSearchFilterAdd(this)"
                    )
                    input (
                        type : "button",
                        class : "bill99scm-smallBT forOrder",
                        value : Messages.SCMRecordsSearchAddOrder(),
                        onclick : "scmSearchOrderAdd(this)"
                    )
                    input (
                        type : "button",
                        class : "bill99scm-smallBT forField",
                        value : Messages.SCMRecordsSearchAddField(),
                        onclick : "scmSearchFieldAdd(this)"
                    )
                    input (
                        type : "button",
                        class : "bill99scm-smallBT submit",
                        value : Messages.SCMCommonSearch(),
                        onclick : "scmSearchFilterApply(this)"
                    )
                    form( method : "POST", action : rootURL + "/" + my.getUrl() ) {
                        input(type : "hidden", name : "filter", value : "")
                        input(type : "hidden", name : "orders", value : "")
                        input(type : "hidden", name : "display", value : "")
                        input(type : "hidden", name : "page", value : my.pager.pageNumber)
                        input(type : "hidden", name : "rows", value : my.pager.perPageSize)
                    }
                    ul( class : "bill99scm-hidden" ) {
                        my.filters.each() { f ->
                            li(
                                field : f.field.getName(),
                                operator : f.operator,
                                display : f.field.getDisplay()
                            ) {
                                f.getValues().each() { v ->
                                    font() {text(v)}
                                }
                            }
                        }
                        my.orders.each() { o ->
                            li(
                                field : o.field.getName(),
                                order : o.order.toString(),
                                display : o.field.getDisplay()
                            ) {
                               text(o.order.display());
                            }
                        }
                        my.displayFields.each() { f ->
                            li(
                                field : f.getName(),
                                displayed : f.getDisplay()
                            )
                        }
                    }
                }
            }
            tr(class : "bill99scm-hidden filter row") {
                td (class : "delete") {
                    input (
                        type : "button",
                        class : "bill99scm-smallBT",
                        value : Messages.SCMSearchFilterItemDelete(),
                        onclick : "scmSearchFilterDelete(this)"
                    )
                }
                td (class : "field") {
                
                }
                td (class : "operator") {
                    select (
                        class : "opsel",
                        onchange : "scmSearchFilterOpChanged(this)"
                    )
                }
                td (class : "values") {
                    input (
                        type : "button",
                        title : Messages.SCMRecordsFilterValueInfo(),
                        class : "valAdd bill99scm-smallBT",
                        value : Messages.SCMRecordsFilterValueAdd(),
                        onclick : "scmSearchFilterValAdd(this)"
                    )
                }
            }
            tr(class : "bill99scm-hidden order row") {
                td (class : "delete") {
                    input (
                        type : "button",
                        class : "bill99scm-smallBT",
                        value : Messages.SCMSearchOrderItemDelete(),
                        onclick : "scmSearchFilterDelete(this)"
                    )
                }
                td (class : "field") {
                
                }
                td (class : "order", colspan : 2) {
                    select ( name : "order", class : "order" ) {
                        SearchOrder.ORDER.values().each() { o ->
                            option( value:o.toString() ) {
                                text(o.display())
                            }
                        }
                    }
                }
            }
        }
        table(class:"bill99scm-table bill99scm-width96", cellspacing:"0", cellpadding:"0") {
            tr() {
                td(class : "parger", colspan : my.displayFields.size()) {
                    drawPager(my.pager, "RecordsList")
                }
            }
            tr() {
                my.displayFields.each() { f ->
                    th() {
                        text(f.getDisplay())
                    }
                }
            }
            my.records.each() { r ->
                tr() {
                    my.displayFields.each() { f ->
                        td() {
                            def target = f.getTarget();
                            def value = r.get(f.getName());
                            def name = target.form.getName();
                            if ( target.isId() && value != null ) {
                                href = rootURL + "/bill99scm/" + name + "/" + value
                                a(class : "bill99scm-standard-link", href : href) {
                                   text(sprintf("%08d", Utils.parseLong(value)))
                                }
                            } else if ( type.rawIsDateTime() ) {
                                text(target.type.display(target.type.rawToObject(target, value), target));
                            } else {
                                text(value);
                            }
                        }
                    }
                }
            }
            if ( my.pager.currentPageSize > 30 ) {
                tr() {
                    td(class : "parger", colspan : my.displayFields.size()) {
                        drawPager(my.pager, "RecordsList")
                    }
                }
            }
        }
    }
}
          