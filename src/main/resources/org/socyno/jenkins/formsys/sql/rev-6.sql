/**
 * Add form others deployment
 *
 */
DROP TABLE IF EXISTS FORM__DPM_OTHERS;
CREATE TABLE FORM__DPM_OTHERS (
    `ID`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `APPLICATION`           VARCHAR(32) NOT NULL,
    `TITLE`                 VARCHAR(255),
    `ATTACHMENTS`           VARCHAR(255) DEFAULT NULL,
    `DETAILS`               LONGTEXT,
    `RESTART`               VARCHAR(32) NOT NULL,
    `STATUS`                VARCHAR(32) NOT NULL,
    `SUBMITTER`             VARCHAR(64) NOT NULL,
    `SUBMITTED`             BIGINT NOT NULL,
    `DEPLOYER`              VARCHAR(64),
    `DEPLOYED`              BIGINT,
    `DEPLOYRS`              VARCHAR(32)
);
        
-- Form for DPM_OTHERS
DELETE FROM FORMS WHERE `NAME` = 'DPM_OTHERS';
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_OTHERS', '其它部署', 'ID,APPLICATION.NAME,TITLE,DEPLOYRS,SUBMITTER,SUBMITTED');
-- Fields of DPM_OTHERS
DELETE FROM `FIELDS` WHERE `FORM` = 'DPM_OTHERS';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`) 
        VALUES ('DPM_OTHERS', 'APPLICATION', '应用名称', 'STRING', 10, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_OTHERS', 'TITLE', '变更简述', 'STRING', 20, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `HELPTEXT`)
        VALUES ('DPM_OTHERS', 'DETAILS', '变更详述', 'TEXT', 30, 1, 1, '请在此处详细说明变更需求，包括操作目的以及操作注意事项。');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_OTHERS', 'RESTART', '是否重启应用', 'STRING', 40, 1, 1, '"no"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_OTHERS', 'ATTACHMENTS', '附件', 'ATTACHMENTS', 45, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_OTHERS', 'SUBMITTER', '提交人', 'STRING', 50, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_OTHERS', 'SUBMITTED', '提交时间', 'DATETIME', 55, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_OTHERS', 'DEPLOYER', '部署人', 'STRING', 60, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_OTHERS', 'DEPLOYED', '部署时间', 'DATETIME', 65, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_OTHERS', 'DEPLOYRS', '部署结果', 'STRING', 70, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_OTHERS', 'STATUS', '状态', 'STRING', 75, 0, 1, 1, 1, 'SELECT');

-- Actions for DPM_OTHERS
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_OTHERS';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_OTHERS', 'new',
            '
["SUBMITTER", "SUBMITTED", "DEPLOYER", "DEPLOYED", "DEPLOYRS"].each() { f->
    record.getForm().getField(f).hidden =true
}
','
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.setFieldValue("STATUS", "submitted", true)
record.setFieldValue("SUBMITTER", Users.getCurrentSysUser().getId(), true)
record.setFieldValue("SUBMITTED", new Date(), true)
','
record.sendEmail(
    "新的部署申请已被提交，请及时处理",
    "新的部署申请已被提交，请及时处理。部署申请表单见下：",
    ["xiaojun.cai"],
    record.getFieldValue("SUBMITTER"),
    "xiaojun.cai"
)
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`)
        VALUES ('DPM_OTHERS', 'view',
            '
["STATUS"].each() { f->
    record.getForm().getField(f).hidden = false
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_OTHERS', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_OTHERS', 'deploy', '标记为已部署', '
return ((String)record.getFieldValue("STATUS")).equals("submitted")
', '
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.getForm().getFields().toArray().each() { f ->
    if ( f.getName() != "DEPLOYRS" ) {
        f.readonly = true
    } else {
        f.readonly = false
    }
}
record.setFieldValue("STATUS", "deployed", true)
record.setFieldValue("DEPLOYER", Users.getCurrentSysUser().getId(), true)
record.setFieldValue("DEPLOYED", new Date(), true)
', '
def checkedField = record.getForm().getFields().get("DEPLOYRS");
checkedField.type.checkRequired(checkedField, record.getFieldValue(checkedField));
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
        VALUES ('DPM_OTHERS', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');

 
-- Options of DPM_OTHERS
DELETE FROM `OPTIONS` WHERE `FORM` = 'DPM_OTHERS';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'DEPLOYRS', 'success');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'RESTART', 'no');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_OTHERS', 'RESTART', 'yes');
        
 