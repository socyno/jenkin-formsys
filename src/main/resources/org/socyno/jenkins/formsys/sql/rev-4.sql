/**
 * Add form message queue services
 *
 */
DROP TABLE IF EXISTS FORM__MQ_SERVICES;
CREATE TABLE FORM__MQ_SERVICES (
    `ID`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `NAME`          VARCHAR(255) NOT NULL,
    `SERVER`        VARCHAR(255) NOT NULL,
    `PORT`          BIGINT NOT NULL,
    `USER`          VARCHAR(255),
    `PASSWD`        VARCHAR(255),
    `COMMENT`       LONGTEXT
);
CREATE UNIQUE INDEX `UK_FORM__MQ_SERVICES_1` ON `FORM__MQ_SERVICES` (`NAME`);

DROP TABLE IF EXISTS FORM__DPM_MESGQUEUE;
CREATE TABLE FORM__DPM_MESGQUEUE (
    `ID`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `OPERATION`     VARCHAR(255) NOT NULL,
    `OBJECT`        VARCHAR(255),
    `BATCH`         VARCHAR(255),
    `PERSISTENCE`   VARCHAR(32),
    `SERVICE`       BIGINT NOT NULL,
    `EXECORDER`     BIGINT,
    `RETRYCOUNT`    BIGINT,
    `MQNAME`        LONGTEXT,
    `COMMENT`       LONGTEXT,
    `MQSIZE`        BIGINT,
    `MQSIZEUNIT`    VARCHAR(255),
    `EXPIRED`       BIGINT,
    `EXPIREDUNIT`   VARCHAR(255),
    `PARAMETERS`    LONGTEXT,
    `STATUS`        VARCHAR(255) NOT NULL,
    `SUBMITTER`     VARCHAR(255) NOT NULL,
    `SUBMITTED`     BIGINT NOT NULL,
    `DEPLOYER`      VARCHAR(255),
    `DEPLOYED`      BIGINT,
    `DEPLOYRS`      VARCHAR(255)
);

-- From for MQ_SERVICES
DELETE FROM FORMS WHERE `NAME` = 'MQ_SERVICES';
INSERT INTO FORMS (`NAME`, `DISPLAY`)
        VALUES ('MQ_SERVICES', '注册的消息服务');
-- Fields of MQ_SERVICES
DELETE FROM FIELDS WHERE `FORM` = 'MQ_SERVICES';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'NAME', '名称', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'SERVER', '服务主机', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'PORT', '端口', 'INTEGER');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'USER', '用户名', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'PASSWD', '密码', 'STRING');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`)
        VALUES ('MQ_SERVICES', 'COMMENT', '备注', 'TEXT');  
 
-- Fields of DPM_MESGQUEUE
DELETE FROM FORMS WHERE `NAME` = 'DPM_MESGQUEUE';
INSERT INTO FORMS (`NAME`, `DISPLAY`, `LISTFIELDS`)
        VALUES ('DPM_MESGQUEUE', 'MQ 部署', 'ID,STATUS,DEPLOYRS,OPERATION,OBJECT,SUBMITTER,SUBMITTED');
DELETE FROM FIELDS WHERE `FORM` = 'DPM_MESGQUEUE';
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', '操作类型', 'STRING', 10, 0, 1, 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', '操作对象', 'STRING', 11, 0, 0, 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `DEFAULT`)
        VALUES ('DPM_MESGQUEUE', 'PERSISTENCE', '持久化', 'STRING', 12, 1, 0, 'SELECT', '"no"');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`)
        VALUES ('DPM_MESGQUEUE', 'BATCH', '批处理名称', 'STRING', 20, 1, 0, 'SELECT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`)
        VALUES ('DPM_MESGQUEUE', 'MQNAME', 'MQ 名称', 'TEXT', 30, 1, 1, 'DEFAULT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `REFERRED`, `ORDER`, `BLOCKED`, `REQUIRED`, `REFERREDCOLUMNS`) 
        VALUES ('DPM_MESGQUEUE', 'SERVICE', '队列服务', 'REFERENCE', 'MQ_SERVICES', 40, 1, 1, 'NAME,SERVER,PORT');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'EXECORDER', '执行顺序', 'INTEGER', 40, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'RETRYCOUNT', '消息重试次数', 'INTEGER', 50, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'MQSIZE', '队列大小', 'INTEGER', 60, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `FORMHEAD`, `INPUTYPE`, `DEFAULT`)
        VALUES ('DPM_MESGQUEUE', 'MQSIZEUNIT', '队列大小单位', 'STRING', 61, 0, 0, 'SELECT', '"kb"');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'EXPIRED', '消息过期时间', 'INTEGER', 70, 0);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `FORMHEAD`, `INPUTYPE`, `DEFAULT`)
        VALUES ('DPM_MESGQUEUE', 'EXPIREDUNIT', '消息过期时间单位', 'STRING', 71, 0, 0, 'SELECT', '"hours"');
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'PARAMETERS', '参数', 'TEXT', 80, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`)
        VALUES ('DPM_MESGQUEUE', 'COMMENT', '备注', 'TEXT', 90, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_MESGQUEUE', 'SUBMITTER', '提交人', 'STRING', 94, 0, 1, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`)
        VALUES ('DPM_MESGQUEUE', 'SUBMITTED', '提交时间', 'DATETIME', 95, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_MESGQUEUE', 'DEPLOYER', '部署人', 'STRING', 100, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `READONLY`)
        VALUES ('DPM_MESGQUEUE', 'DEPLOYED', '部署时间', 'DATETIME', 105, 0, 0, 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `REQUIRED`, `INPUTYPE`, `READONLY`)
        VALUES ('DPM_MESGQUEUE', 'DEPLOYRS', '部署结果', 'STRING', 110, 0, 0, 'SELECT', 1);
INSERT INTO FIELDS (`FORM`, `NAME`, `DISPLAY`, `TYPE`, `ORDER`, `BLOCKED`, `READONLY`, `REQUIRED`, `HIDDEN`, `INPUTYPE`)
        VALUES ('DPM_MESGQUEUE', 'STATUS', '状态', 'STRING', 120, 0, 1, 1, 1, 'SELECT');

-- Actions for DPM_MESGQUEUE
DELETE FROM `ACTIONS` WHERE `FORM` = 'DPM_MESGQUEUE';
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_MESGQUEUE', 'new',
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
        VALUES ('DPM_MESGQUEUE', 'view',
            '
fields = record.getForm().getFields()
["STATUS"].each() { f->
    field = fields.get(f)
    field.hidden = false
    field.readonly = true
}
');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `CONSCRIPT`)
        VALUES ('DPM_MESGQUEUE', 'edit', 'return false');
INSERT INTO `ACTIONS` (`FORM`, `NAME`, `DISPLAY`, `CONSCRIPT`, `PRESCRIPT`, `VRFSCRIPT`, `POSSCRIPT`)
        VALUES ('DPM_MESGQUEUE', 'deploy', '标记为已部署', '
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
        VALUES ('DPM_MESGQUEUE', 're-apply', '修正并重提', '
return ((String)record.getFieldValue("STATUS")).equals("deployed") && ((String)record.getFieldValue("DEPLOYRS")).equals("failure")
', ':new', ':new', ':new');


-- Options for DPM_MESGQUEUE
DELETE FROM OPTIONS WHERE FORM = 'DPM_MESGQUEUE';
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', 'create');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', 'delete');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', 'addprop');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', 'setprop');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OPERATION', 'other');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'queue');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'topic');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'route');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'factory');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'jndiname');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'rvcmlistener');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'durable');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'connection');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'message');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'bridge');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'group');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'OBJECT', 'user');

INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'PERSISTENCE', 'no');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'PERSISTENCE', 'yes');

INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'MQSIZEUNIT', 'KB');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'MQSIZEUNIT', 'MB');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'MQSIZEUNIT', 'GB');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'EXPIREDUNIT', 'millisecond(s)');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'EXPIREDUNIT', 'second(s)');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'EXPIREDUNIT', 'hour(s)');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'EXPIREDUNIT', 'day(s)');
        
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'STATUS', 'submitted');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'STATUS', 'deployed');
        
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'DEPLOYRS', 'failure');
INSERT INTO OPTIONS (`FORM`, `FIELD`, `VALUE`)
        VALUES ('DPM_MESGQUEUE', 'DEPLOYRS', 'success');

