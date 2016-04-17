/**
 * Add form APP package deployment
 *
 */

DROP TABLE IF EXISTS FORM__DPM_APPLICATIONS
CREATE TABLE FORM__DPM_APPLICATIONS (
    `ID`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `APPLICATION`           VARCHAR(32) NOT NULL,
    `VCSPATH`               VARCHAR(255),
    `PACKAGE`               BIGINT DEFAULT NULL,
    `COMMENT`               LONGTEXT,
    `STATUS`                VARCHAR(32) NOT NULL,
    `SUBMITTER`             VARCHAR(64) NOT NULL,
    `SUBMITTED`             BIGINT NOT NULL,
    `DEPLOYER`              VARCHAR(64),
    `DEPLOYED`              BIGINT,
    `DEPLOYRS`              VARCHAR(32)
);
        
-- Form for DPM_APPLICATIONS
DELETE FROM `FORMS` WHERE `NAME` = 'DPM_APPLICATIONS';
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_APPLICATIONS', '应用部署', 'ID,STATUS,APPLICATION.NAME,DEPLOYRS,SUBMITTER,SUBMITTED');
-- Fields of DPM_APPLICATIONS
DELETE FROM `FIELDS` WHERE `FORM` = 'DPM_APPLICATIONS';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`) 
        VALUES ('DPM_APPLICATIONS', 'APPLICATION', '应用名称', 'STRING', 10, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_APPLICATIONS', 'VCSPATH', '代码路径', 'STRING', 20, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_APPLICATIONS', 'PACKAGE', '应用WAR包', 'ATTACHMENT', 30, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_APPLICATIONS', 'COMMENT', '备注', 'TEXT', 40, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_APPLICATIONS', 'SUBMITTER', '提交人', 'STRING', 50, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_APPLICATIONS', 'SUBMITTED', '提交时间', 'DATETIME', 55, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_APPLICATIONS', 'DEPLOYER', '部署人', 'STRING', 60, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_APPLICATIONS', 'DEPLOYED', '部署时间', 'DATETIME', 65, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_APPLICATIONS', 'DEPLOYRS', '部署结果', 'STRING', 70, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_APPLICATIONS', 'STATUS', '状态', 'STRING', 75, 0, 1, 1, 1, 'SELECT');

-- Actions for DPM_APPLICATIONS
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_APPLICATIONS';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_APPLICATIONS', 'new',
            '
["SUBMITTER", "SUBMITTED", "DEPLOYER", "DEPLOYED", "DEPLOYRS"].each() { f->
    record.getForm().getField(f).hidden = true
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
        VALUES ('DPM_APPLICATIONS', 'view',
            '
["STATUS"].each() { f->
    record.getForm().getField(f).hidden = false
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_APPLICATIONS', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_APPLICATIONS', 'deploy', '标记为已部署', '
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
        VALUES ('DPM_APPLICATIONS', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');

-- Options of DPM_APPLICATIONS
DELETE FROM `OPTIONS` WHERE `FORM` = 'DPM_APPLICATIONS';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_APPLICATIONS', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_APPLICATIONS', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_APPLICATIONS', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_APPLICATIONS', 'DEPLOYRS', 'success');

 