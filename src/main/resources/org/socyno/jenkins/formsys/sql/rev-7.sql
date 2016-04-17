/**
 * Add form data source change
 *
 */
DROP TABLE `FORM__DPM_DATASOURCES`;
CREATE TABLE `FORM__DPM_DATASOURCES` (
    `ID`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `APPLICATION`           VARCHAR(32) NOT NULL,
    `JNDINAME`              VARCHAR(255),
    `DBSCHEMA`              BIGINT NOT NULL,
    `POOLSIZE`              BIGINT NOT NULL DEFAULT 10,
    `TRMANAGER`             VARCHAR(32) NOT NULL,
    `OPERATION`             VARCHAR(32) NOT NULL,
    `RESTART`               VARCHAR(32) NOT NULL,
    `STATUS`                VARCHAR(32) NOT NULL,
    `SUBMITTER`             VARCHAR(64) NOT NULL,
    `SUBMITTED`             BIGINT NOT NULL,
    `DEPLOYER`              VARCHAR(64),
    `DEPLOYED`              BIGINT,
    `DEPLOYRS`              VARCHAR(32)
);
        
-- Form for DPM_DATASOURCES
DELETE FROM `FORMS` WHERE `NAME` = 'DPM_DATASOURCES';
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_DATASOURCES', '数据源变更', 'ID,APPLICATION.NAME,STATUS,DEPLOYRS,JNDINAME,SUBMITTER,SUBMITTED');
-- Fields of DPM_DATASOURCES
DELETE FROM `FIELDS` WHERE `FORM` = 'DPM_DATASOURCES';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`) 
        VALUES ('DPM_DATASOURCES', 'APPLICATION', '应用', 'STRING', 10, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `HELPTEXT`)
        VALUES ('DPM_DATASOURCES', 'JNDINAME', 'JNDI 名称', 'STRING', 20, 1, 1, '参照格式 ：BILL99/JDBC/SEASHELL');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `REFERRED`, `ORDER`, `BLOCKED`, `REQUIRED`, `REFERREDCOLUMNS`) 
        VALUES ('DPM_DATASOURCES', 'DBSCHEMA', '数据库', 'REFERENCE', 'DB_SCHEMAS', 20, 1, 1, 'SERVICE.TYPE,NAME');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `HELPTEXT`)
        VALUES ('DPM_DATASOURCES', 'POOLSIZE', '最大连接池', 'INTEGER', 30, 1, 1, 'return 10', '默认是10，若修改，请先咨询DBA是否有必要修改。');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_DATASOURCES', 'TRMANAGER', '事物管理器', 'STRING', 33, 1, 1, 'return "c3p0"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_DATASOURCES', 'OPERATION', '操作类型', 'STRING', 36, 1, 1, 'return "add"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `DEFAULT`, `INPUTYPE`)
        VALUES ('DPM_DATASOURCES', 'RESTART', '是否重启应用', 'STRING', 40, 1, 1, '"yes"', 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATASOURCES', 'SUBMITTER', '提交人', 'STRING', 50, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATASOURCES', 'SUBMITTED', '提交时间', 'DATETIME', 55, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATASOURCES', 'DEPLOYER', '部署人', 'STRING', 60, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATASOURCES', 'DEPLOYED', '部署时间', 'DATETIME', 65, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_DATASOURCES', 'DEPLOYRS', '部署结果', 'STRING', 70, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_DATASOURCES', 'STATUS', '状态', 'STRING', 75, 0, 1, 1, 1, 'SELECT');

-- Actions for DPM_DATASOURCES
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_DATASOURCES';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_DATASOURCES', 'new',
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
    record.getFieldValue("SUBMITTER")
)
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`)
        VALUES ('DPM_DATASOURCES', 'view',
            '
["STATUS"].each() { f->
    record.getForm().getField(f).hidden = false
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_DATASOURCES', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_DATASOURCES', 'deploy', '标记为已部署', '
return ((String)record.getFieldValue("STATUS")).equals("submitted")
', '
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.getForm().getFields().toArray().each() { f ->
    if ( f.getName() != "DEPLOYRS" ) {
        f.required = false
        f.readonly = true
    } else {
        f.required = true
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
        VALUES ('DPM_DATASOURCES', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');

 
-- Options of DPM_DATASOURCES
DELETE FROM `OPTIONS` WHERE `FORM`='DPM_DATASOURCES';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'DEPLOYRS', 'success');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'RESTART', 'no');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'RESTART', 'yes');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'OPERATION', 'add');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'OPERATION', 'update');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'OPERATION', 'delete');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'TRMANAGER', 'c3p0');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATASOURCES', 'TRMANAGER', 'atomikos');
        
 