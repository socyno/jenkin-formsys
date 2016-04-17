
-- Unique keys of DB_SERVICES
INSERT INTO `UNIQUES` (`FORM`, `NAME`, `FIELD`)
        VALUES ('DB_SERVICES', 'unique', 2);

SELECT `ID`, `NAME` FROM `FIELDS` WHERE `FORM`='DB_SCHEMAS';
INSERT INTO `UNIQUES` (`FORM`, `NAME`, `FIELD`, `ORDER`)
        VALUES ('DB_SCHEMAS', 'unique', 5, 20);
INSERT INTO `UNIQUES` (`FORM`, `NAME`, `FIELD`, `ORDER`)
        VALUES ('DB_SCHEMAS', 'unique', 6, 10);
INSERT INTO `UNIQUES` (`FORM`, `NAME`, `FIELD`, `ORDER`)
        VALUES ('DB_SCHEMAS', 'unique', 7, 30);
        
-- Sample data of DB_SERVICES
INSERT INTO `FORM__DB_SERVICES` (`TYPE`, `NAME`, `ADDRESS`)
        VALUES ('mysql', 'TEST001', 'mysql://asdfsf');
INSERT INTO `FORM__DB_SERVICES` (`TYPE`, `NAME`, `ADDRESS`)
        VALUES ('oracle', 'TEST002', 'oracle://asdfsf');

-- Sample data of DB_SCHEMAS
INSERT INTO `FORM__DB_SCHEMAS` (`NAME`, `SERVICE`, `USERNAME`)
        VALUES ('s001', 1, 'u001');
INSERT INTO `FORM__DB_SCHEMAS` (`NAME`, `SERVICE`, `USERNAME`)
        VALUES ('s002', 2, 'u002');
        