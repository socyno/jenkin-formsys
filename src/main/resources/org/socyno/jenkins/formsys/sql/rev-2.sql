/**
 * Add form DB_SERVICES, DB_SCHEMAS and DPM_DATABASES
 *
 */

CREATE TABLE IF NOT EXISTS FORM__DB_SERVICES (
    `ID`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `TYPE`          VARCHAR(128) NOT NULL,
    `NAME`          VARCHAR(128) NOT NULL,
    `ADDRESS`       VARCHAR(255) NOT NULL,
    `DESCRIPTION`   LONGTEXT
);
CREATE UNIQUE INDEX `UK_FORM__DB_SERVICES_1` ON `FORM__DB_SERVICES` (`NAME`);

CREATE TABLE IF NOT EXISTS FORM__DB_SCHEMAS (
    `ID`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `NAME`          VARCHAR(255) NOT NULL,
    `SERVICE`       BIGINT NOT NULL,
    `USERNAME`      VARCHAR(128) NOT NULL,
    `PASSWORD`      VARCHAR(128),
    `PROPERTIES`    TEXT
);
CREATE UNIQUE INDEX `UK_FORM__DB_SCHEMAS_1` ON `FORM__DB_SCHEMAS` (`SERVICE`, `NAME`, `USERNAME`);

CREATE TABLE IF NOT EXISTS FORM__DPM_DATABASES (
    `ID`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `OPERATION`     VARCHAR(128) NOT NULL,
    `DBSCHEMA`      BIGINT NOT NULL,
    `EXECORDER`     INT DEFAULT -1,
    `CONTENTS`      LONGTEXT,
    `ROLLBACK`      LONGTEXT,
    `COMMENT`       LONGTEXT,
    `ATTACHMENTS`   VARCHAR(255),
    `STATUS`        VARCHAR(127) NOT NULL,
    `SUBMITTER`     VARCHAR(127) NOT NULL,
    `SUBMITTED`     BIGINT NOT NULL,
    `DEPLOYER`      VARCHAR(127) DEFAULT NULL,
    `DEPLOYED`      BIGINT DEFAULT NULL,
    `DEPLOYRS`      VARCHAR(127) DEFAULT NULL
);

-- From for DB_SERVICES
INSERT INTO FORMS (`NAME`, `DISPLAY`)
        VALUES ('DB_SERVICES', '数据库服务');
-- Fields of DB_SERVICES
DELETE FROM FIELDS WHERE `FORM` = 'DB_SERVICES';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SERVICES', 'NAME', '名称', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SERVICES', 'TYPE', '类型', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SERVICES', 'ADDRESS', '链接地址', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SERVICES', 'DESCRIPTION', '描述信息', 'TEXT');    
-- Options for DB_SERVICES
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`, `DISPLAY`)
        VALUES ('DB_SERVICES', 'TYPE', 'mysql', 'MySQL');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`, `DISPLAY`)
        VALUES ('DB_SERVICES', 'TYPE', 'oracle', 'Oracle');

-- From for DB_SCHEMAS
INSERT INTO FORMS (`NAME`, `DISPLAY`)
        VALUES ('DB_SCHEMAS', '注册数据库');
-- Fields of DB_SCHEMAS
DELETE FROM FIELDS WHERE `FORM` = 'DB_SCHEMAS';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SCHEMAS', 'NAME', '名称', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `REFERRED`)
        VALUES ('DB_SCHEMAS', 'SERVICE', '服务', 'REFERENCE', 'DB_SERVICES');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SCHEMAS', 'USERNAME', '用户名', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SCHEMAS', 'PASSWORD', '密码', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('DB_SCHEMAS', 'PROPERTIES', '连接属性', 'STRING');

        
-- Fields of DPM_DATABASES
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_DATABASES', 'CR 部署', 'ID,STATUS,DEPLOYRS,OPERATION,DBSCHEMA,EXECORDER,CONTENTS,ROLLBACK,COMMENT,SUBMITTER,SUBMITTED');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`)
        VALUES ('DPM_DATABASES', 'OPERATION', '操作类型', 'STRING', 10, 0, 1, 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `REFERRED`, `ORDER`, `BLOCKED`, `REQUIRED`, `REFERREDCOLUMNS`) 
        VALUES ('DPM_DATABASES', 'DBSCHEMA', '数据库名称', 'REFERENCE', 'DB_SCHEMAS', 20, 1, 1, 'ID,NAME,SERVICE.TYPE, SERVICE.ADDRESS');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_DATABASES', 'EXECORDER', '执行顺序', 'INTEGER', 40, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATABASES', 'CONTENTS', '执行语句', 'TEXT', 50, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATABASES', 'ROLLBACK', '回滚语句', 'TEXT', 60, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATABASES', 'COMMENT', '备注', 'TEXT', 70, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATABASES', 'ATTACHMENTS', '附件', 'ATTACHMENTS', 80, 1, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATABASES', 'SUBMITTER', '提交人', 'STRING', 90, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_DATABASES', 'SUBMITTED', '提交时间', 'DATETIME', 95, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATABASES', 'DEPLOYER', '部署人', 'STRING', 100, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_DATABASES', 'DEPLOYED', '部署时间', 'DATETIME', 105, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_DATABASES', 'DEPLOYRS', '部署结果', 'STRING', 110, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_DATABASES', 'STATUS', '状态', 'STRING', 120, 0, 1, 1, 1, 'SELECT');
        
-- Actions for DPM_DATABASES
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_DATABASES';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_DATABASES', 'new',
            '
["SUBMITTER", "SUBMITTED", "DEPLOYER", "DEPLOYED", "DEPLOYRS"].each() { f->
    record.getForm().getField(f).hidden = true
}
','
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.setFieldValue("STATUS", "submitted", true)
record.setFieldValue("SUBMITTER", Users.getCurrent().getName(), true)
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
        VALUES ('DPM_DATABASES', 'view',
            '
fields = record.getForm().getFields()
["STATUS"].each() { f->
    field = fields.get(f)
    field.hidden = false
    field.readonly = true
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_DATABASES', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_DATABASES', 'deploy', '标记为已部署', '
return ((String)record.getFieldValue("STATUS")).equals("submitted")     
', '
import java.util.Date
import org.socyno.jenkins.formsys.Users
record.getForm().getFields().toArray().each() { f ->
    f.readonly = f.getName() != "DEPLOYRS"
}
record.setFieldValue("STATUS", "deployed", true)
record.setFieldValue("DEPLOYER", Users.getCurrentSysUser().getId(), true)
record.setFieldValue("DEPLOYED", new Date(), true)
', '
def checkedField = record.getForm().getFields().get("DEPLOYRS");
checkedField.type.checkRequired(checkedField, record.getFieldValue(checkedField));
', '
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
        VALUES ('DPM_DATABASES', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');


-- Options for DPM_DATABASES
DELETE FROM OPTIONS WHERE `FORM` = 'DPM_DATABASES';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'OPERATION', '01-普通');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'OPERATION', '02-Schema 或表空间处理');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'OPERATION', '03-SQLLDR 导入');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'OPERATION', '04-权限导入');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_DATABASES', 'DEPLOYRS', 'success');

