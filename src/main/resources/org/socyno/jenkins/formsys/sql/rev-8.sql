/**
 * Add form parameters change
 *
 */

DROP TABLE FORM__DPM_PARAMETERS;
CREATE TABLE FORM__DPM_PARAMETERS (
    `ID`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `APPLICATION`           VARCHAR(32) NOT NULL,
    `PARAMNAME`             VARCHAR(64),
    `PARAMVALUE`            VARCHAR(255),
    `PRODUCT`               VARCHAR(32),
    `OPERATION`             VARCHAR(32) NOT NULL,
    `COMMENT`               LONGTEXT,
    `ENCRYPT`               VARCHAR(32) NOT NULL,
    `RESTART`               VARCHAR(32) NOT NULL,
    `INPUTYPE`              VARCHAR(32) NOT NULL,
    `ATTACHMENTS`           VARCHAR(255) DEFAULT NULL,
    `STATUS`                VARCHAR(32) NOT NULL,
    `SUBMITTER`             VARCHAR(64) NOT NULL,
    `SUBMITTED`             BIGINT NOT NULL,
    `DEPLOYER`              VARCHAR(64),
    `DEPLOYED`              BIGINT,
    `DEPLOYRS`              VARCHAR(32)
);

-- Form for DPM_PARAMETERS
DELETE FROM FORMS WHERE `NAME` = 'DPM_PARAMETERS';
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_PARAMETERS', '参数平台变更', 'ID,APPLICATION.NAME,DEPLOYRS,SUBMITTER,SUBMITTED');
-- Fields of DPM_PARAMETERS
DELETE FROM FIELDS WHERE `FORM` = 'DPM_PARAMETERS';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`) 
        VALUES ('DPM_PARAMETERS', 'APPLICATION', '应用名称', 'STRING', 10, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `DEFAULT`, `INPUTYPE`, `REQUIRED`, `HELPTEXT`)
        VALUES ('DPM_PARAMETERS', 'INPUTYPE', '输入方式', 'STRING', 20, 1, '"input"','SELECT', 1, '如果从文件导入，请选择 "import"，并上传 EXCEL文件即可');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_PARAMETERS', 'PARAMNAME', '参数名', 'STRING', 30, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `HELPTEXT`)
        VALUES ('DPM_PARAMETERS', 'PARAMVALUE', '参数值', 'STRING', 40, 1, '若参数值不填，则代表此参数的值是空值！');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`) 
        VALUES ('DPM_PARAMETERS', 'PRODUCT', '产品 ID', 'STRING', 50, 0, 1, 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_PARAMETERS', 'OPERATION', '操作类型', 'STRING', 60, 1, 1, 'return "add"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_PARAMETERS', 'ENCRYPT', '是否加密', 'STRING', 70, 1, 1, '"no"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_PARAMETERS', 'RESTART', '是否重启应用', 'STRING', 80, 1, 1, '"no"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_PARAMETERS', 'ATTACHMENTS', '附件', 'ATTACHMENTS', 85, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_PARAMETERS', 'SUBMITTER', '提交人', 'STRING', 90, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_PARAMETERS', 'SUBMITTED', '提交时间', 'DATETIME', 95, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_PARAMETERS', 'DEPLOYER', '部署人', 'STRING', 100, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_PARAMETERS', 'DEPLOYED', '部署时间', 'DATETIME', 105, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_PARAMETERS', 'DEPLOYRS', '部署结果', 'STRING', 110, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_PARAMETERS', 'STATUS', '状态', 'STRING', 115, 0, 1, 1, 1, 'SELECT');

-- Actions for DPM_PARAMETERS
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_PARAMETERS';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_PARAMETERS', 'new',
            '
fields = record.getForm().getFields()
["SUBMITTER", "SUBMITTED", "DEPLOYER", "DEPLOYED", "DEPLOYRS"].each() { f->
    fields.get(f).hidden = true
}
["PARAMNAME", "PRODUCT", "RESTART", "ENCRYPT", "OPERATION", "ATTACHMENTS"].each() { f->
    fields.get(f).hidden = false
    fields.get(f).hidden = false
}
','
import java.util.Date
import org.socyno.jenkins.formsys.Users

fields = record.getForm().getFields()
checkField = fields.get("ATTACHMENTS")
if ( ((String)record.getFieldValue("INPUTYPE")).equals("import") ) {
    checkField.type.checkRequired(checkField, record.getFieldValue(checkField));
} else {
    record.setFieldValue(checkField, null)
    ["PARAMNAME", "PRODUCT", "RESTART", "ENCRYPT", "OPERATION"].each() { f->
        checkField = fields.get(f)
        checkField.type.checkRequired(checkField, record.getFieldValue(checkField));
    }
}
record.setFieldValue("STATUS", "submitted", true)
record.setFieldValue("SUBMITTER", Users.getCurrentSysUser().getId(), true)
record.setFieldValue("SUBMITTED", new Date(), true)
','
record.sendEmail(
    "新的部署申请已被提交，请及时处理",
    "新的部署申请已被提交，请及时处理。部署申请表单见下：",
    ["xiaojun.cai"],
    record.getFieldValue("SUBMITTER")
)
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`)
        VALUES ('DPM_PARAMETERS', 'view',
            '
["STATUS"].each() { f->
    record.getForm().getField(f).hidden = false
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_PARAMETERS', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_PARAMETERS', 'deploy', '标记为已部署', '
return ((String)record.getFieldValue("STATUS")).equals("submitted")
', '
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.getForm().getFields().toArray().each() { f ->
    if ( f.getName() != "DEPLOYRS" ) {
        f.readonly = true
        if ( f.getName() == "PRODUCT" ) {
            f.required = false
        }
    } else {
        f.readonly = false
        f.required = true
    }
}
record.setFieldValue("STATUS", "deployed", true)
record.setFieldValue("DEPLOYER", Users.getCurrentSysUser().getId(), true)
record.setFieldValue("DEPLOYED", new Date(), true)
', '
def fResult = record.getForm().getFields().get("DEPLOYRS");
fResult.type.checkRequired(fResult, record.getFieldValue(fResult));
','
dpmRsValue = record.getFieldValue("DEPLOYRS")
record.sendEmail(
    "您的部署申请已被执行，结果为： " + dpmRsValue,
    "您的部署申请已被执行，结果为： " + dpmRsValue + "。 部署申请表单见下：",
    [record.getFieldValue("SUBMITTER")],
    record.getFieldValue("DEPLOYER"),
    "xiaojun.cai"
)
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_PARAMETERS', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');

-- Options of DPM_PARAMETERS
DELETE FROM `OPTIONS` WHERE `FORM` = 'DPM_PARAMETERS';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'DEPLOYRS', 'success');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'RESTART', 'no');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'RESTART', 'yes');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'ENCRYPT', 'no');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'ENCRYPT', 'yes');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'INPUTYPE', 'import');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'INPUTYPE', 'input');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'PRODUCT', 'PRD001');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'OPERATION', 'add');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'OPERATION', 'update');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_PARAMETERS', 'OPERATION', 'delete');
 