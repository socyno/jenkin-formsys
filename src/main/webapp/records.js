Q(document).ready( function() {
    scmSearchFiltersReset();
} )

function scmSearchFiltersReset() {
    var meta = scmParseRecordsMeta();
    Q("table.bill99scm-rsf-form").find("tr.search.row select option").each( function() {
        var value = Q.trim(Q(this).attr("value"));
        var field = value.substring(0, value.indexOf(","));
        for ( var idx = 0; idx < meta.filters.length; idx++ ) {
            if ( meta.filters[idx]["field"] === field ) {
                this.selected = true;
                var jRow = scmSearchFilterAdd(null, idx);
                var operator = meta.filters[idx]["operator"];
                jRow.find("select option").each( function() {
                    if ( Q.trim(Q(this).attr("value")).substring(0, operator.length) === operator ) {
                        this.selected = true;
                        scmSearchFilterOpChanged(jRow.find("select"));
                        return false;
                    }
                } );
                var values = meta.filters[idx]["values"];
                var jVals = jRow.find("td.values");
                for ( i = 0; i < values.length; i++ ) {
                    scmSearchFilterValAdd0(jVals, values[i]);
                }
            }
        }
        for ( var idx = 0; idx < meta.orders.length; idx++ ) {
            if ( meta.orders[idx]["field"] === field ) {
                this.selected = true;
                var jRow = scmSearchOrderAdd(null, idx);
                var order = meta.orders[idx]["order"];
                jRow.find("select").find("option").each( function() {
                    if ( Q.trim(Q(this).attr("value")) === order ) {
                        this.selected = true;
                        return false;
                    }
                } );
            }
        }
        for ( var idx = 0; idx < meta.displayed.length; idx++ ) {
            if ( meta.displayed[idx] === field ) {
                this.selected = true;
                scmSearchFieldAdd(null, idx);
            }
        }
    } );
}

function scmParseRecordsMeta() {
    var meta = {
        filters : [],
        orders : [],
        displayed : []
    };
    Q("table.bill99scm-rsf-form")
            .find("tr.search.row")
            .find("ul:hidden li").each( function() {
        var q = Q(this);
        var field = Q.trim(q.attr("field"));
        var order = Q.trim(q.attr("order"));
        var operator = Q.trim(q.attr("operator"));
        var displayed = Q.trim(q.attr("displayed"));
        var pageSize = Q.trim(q.attr("perPageSize"));
        if ( field != "" && operator != "" ) {
            var values = new Array();
            q.find("font").each( function() {
                values.push(Q(this).text());
            } );
            meta.filters.push( {
                field : field,
                operator : operator,
                values : values
            } );
        } else if ( field != "" && order != "" ) {
            meta.orders.push( {
                field : field,
                order : order
            } );
        } else if ( field != "" && displayed != "" ) {
            meta.displayed.push(field);
        }
    } );
    return meta;
}

function scmPagerRecordsList(elem) {
    var page = parseInt(Q(elem).text(),10);
    if ( isNaN(page) ) {
        return;
    }
    var meta = scmParseRecordsMeta();
    var jForm = Q("table.bill99scm-rsf-form")
        .find("tr.search.row")
        .find("form");
    jForm.find("input[name=page]")
        .val(page);
    jForm.find("input[name=orders]")
        .val(eval(JSON.stringify(meta.orders)));
    jForm.find("input[name=display]")
        .val(eval(JSON.stringify(meta.displayed)));
    jForm.find("input[name=filter]")
        .val(eval(JSON.stringify(meta.filters)));
    jForm.submit();
}

function scmRefreshSearchFormStyle() {
    Q("table.bill99scm-rsf-form").find("tr:visible").each( function(index) {
        Q(this).removeClass("odd").removeClass("even");
        Q(this).addClass(index % 2 == 0 ? "even" : "odd");
    } );
}

function scmSearchFilterDelete(elem) {
    Q(elem).closest('tr').remove();
    scmRefreshSearchFormStyle();
}

function scmSearchFilterAdd(self, type, index) {
    var jSel = Q("table.bill99scm-rsf-form")
            .find("tr.search.row select");
    var val = Q.trim(jSel.val());
    if ( val == "" ) {
        return null;
    }
    var option = ""; 
    jSel.find("option").each( function() {
        if ( Q.trim(Q(this).val()) === val ) {
            option = this;
            return false;
        }
    } );
    jSel.find("option").get(0).selected = true;
    if ( option === "" ) {
        return null;
    }
    var jRow;
    var display = Q(option).text();
    var fldinfos = val.split(",")
    if ( !isNaN(type)  ) {
        index = type;
    }
    if ( type == "order" ) {
        jSel.closest("table").find("tr.order:visible select").each( function() {
            if ( Q(this).attr("name") == fldinfos[0] ) {
                Q(this).closest("tr").remove();
            }
        } );
        var jRows = jSel.closest("table").find("tr.order:visible");
        if ( isNaN(index) ) {
            index = 0;
            if ( jRows.size() > 0 ) {
                index = jRows.last().attr("index") + 1;
            }
        }
        jRow = jSel.closest("table")
            .find("tr.order:hidden").clone()
            .attr("index", index);
        jRow.find("td.field").text(display);
        jRow.find("select").attr("name", fldinfos[0]);
        if ( jRows.size() == 0 || jRows.last().attr("index") < index ) {
            jRow.insertBefore(jSel.closest("table").find("tr.display"));
        } else if ( jRows.first().attr("index") >= index ) {
            jRow.insertBefore(jRows.first());
        } else {
            jRows.each( function() {
                if ( Q(this).attr("index") >= index ) {
                    jRow.insertBefore(this);
                    return false;
                }
            } );
        }
        jRow.show();
    } else if ( type == "field" ) {
        var target = jSel.closest("table")
            .find("tr.display:first td.fields");
        var jItems = target.find("font:visible");
        if ( isNaN(index) ) {
            index = 0;
            if ( jItems.size() > 0 ) {
                index = jItems.last().attr("index") + 1;
            }
        }
        var jItem = Q('<font></font>')
            .attr("index", index)
            .attr("value", fldinfos[0])
            .text(display)
            .addClass("value");
        if ( jItems.size() == 0 || jItems.last().attr("index") < index ) {
            jItem.appendTo(target);
        } else if ( jItems.first().attr("index") >= index ) {
            jItem.prependTo(target);
        } else {
            jItems.each( function() {
                if ( Q(this).attr("index") >= index ) {
                    jItem.insertBefore(this);
                    return false;
                }
            } );
        }
        Q(' ').insertAfter(jItem);
        Q('<a href="javascript:void(0)">X</a>')
            .addClass("delete")
            .attr("title", "Delete")
            .appendTo(jItem).click( function() {
                Q(this).closest("font").remove();
            } );
    } else {
        jRow = jSel.closest("table")
            .find("tr.filter:hidden").clone();
        jRow.find("td.field").text(display);
        var jOps = jRow.find("select.opsel");
        for ( var i = 1; i < fldinfos.length; i++ ) {
            var vs = fldinfos[i].split("|", 3);
            Q('<option></option>')
                    .text(vs[2])
                    .val(vs[0] + vs[1])
                    .appendTo(jOps);
        }
        var target = jSel.closest("table").find("tr.display");
        if ( target.siblings("tr.order:visible").size() > 0 ) {
            target = target.siblings("tr.order:first");
        }
        jRow.insertBefore(target).show();
        scmSearchFilterOpChanged(jOps.attr("name", fldinfos[0]))
    }
    scmRefreshSearchFormStyle();
    return jRow;
}

function scmSearchOrderAdd(self, index) {
    return scmSearchFilterAdd(self, "order", index);
}

function scmSearchFieldAdd(self, index) {
    return scmSearchFilterAdd(self, "field", index);
}

function scmSearchFilterValAdd(elem, title) {
    var value = window.prompt(Q(elem).attr("title"));
    if ( value !== null ) {
        scmSearchFilterValAdd0(Q(elem).closest("td"), value);
    }
}

function scmSearchFilterValAdd0(elem, val) {
    var exists = false;
    Q(elem).find("font").each( function() {
        if ( Q(this).attr("value") === val ) {
            exists = true;
            return false;
        }
    } );
    if ( !exists ) {
        var jItem = Q('<font></font>')
            .attr("value", val)
            .text(val)
            .addClass("value")
            .appendTo(Q(elem));
        Q(elem).append(' ');
        Q('<a href="javascript:void(0)">X</a>')
            .addClass("delete")
            .appendTo(jItem).click( function() {
                Q(this).closest("font").remove();
            } );
    }
}

function scmSearchFilterOpChanged(elem) {
    var v = Q.trim(Q(elem).val());
    var elem = Q(elem).closest("td")
        .siblings("td.values")
        .children();
    if ( v.substring(v.length - 1) == "N" ) {
        elem.hide();
    } else {
        elem.show();
    }
}

function scmSearchFilterApply(elem) {
    var filters = new Array();
    var selfRow = Q(elem).closest("tr");
    selfRow.siblings("tr.filter:visible").each( function() {
        var jOperator = Q(this).find("select.opsel");
        if ( jOperator.size() <= 0 ) {
            return;
        }
        var values = new Array();
        var field = jOperator.attr("name");
        var operator = jOperator.val();
        operator = operator.substring(0, operator.length - 1)
        Q(this).find(".value").each( function() {
            values.push(Q(this).attr("value"));
        } );
        filters.push( {
            "field" : field,
            "operator" : operator,
            "values" : values
        } )
    } );
    var orders = new Array();
    selfRow.siblings("tr.order:visible").each( function() {
        var q = Q(this).find("select");
        orders.push( {
            "field" : q.attr("name"),
            "order" : q.val()
        } )
    } );
    var fields = new Array();
    selfRow.siblings("tr.display:visible").find(".value").each( function() {
        fields.push(Q(this).attr("value"));
    } );
    
    var jForm = selfRow.find("form");
    jForm.find("input[name=page]")
        .val(1);
    jForm.find("input[name=orders]")
        .val(eval(JSON.stringify(orders)));
    jForm.find("input[name=filter]")
        .val(eval(JSON.stringify(filters)));
    jForm.find("input[name=display]")
        .val(eval(JSON.stringify(fields)));
    jForm.submit();
}