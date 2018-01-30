CREATE TABLE UM_USER (
   UM_ID INTEGER NOT NULL AUTO_INCREMENT,
   UM_USER_NAME VARCHAR(255) NOT NULL,
   UM_USER_PASSWORD VARCHAR(512) NOT NULL,
   UM_CHANGED_TIME TIMESTAMP NOT NULL,
   PRIMARY KEY (UM_ID),
   UNIQUE(UM_USER_NAME)
)ENGINE INNODB;

CREATE TABLE UM_USER_ATTRIBUTE (
    UM_ID INTEGER NOT NULL AUTO_INCREMENT,
    UM_ATTR_NAME VARCHAR(255) NOT NULL,
    UM_ATTR_VALUE VARCHAR(1024),
    UM_USER_ID INTEGER,
    FOREIGN KEY (UM_USER_ID) REFERENCES UM_USER(UM_ID),
    PRIMARY KEY (UM_ID)
)ENGINE INNODB;

CREATE INDEX UM_USER_ID_INDEX ON UM_USER_ATTRIBUTE(UM_USER_ID);

CREATE TABLE UM_SYSTEM_ROLE(
    UM_ID INTEGER NOT NULL AUTO_INCREMENT,
    UM_ROLE_NAME VARCHAR(255),
    PRIMARY KEY (UM_ID)
)ENGINE INNODB;

CREATE INDEX SYSTEM_ROLE_IND_BY_RN_TI ON UM_SYSTEM_ROLE(UM_ROLE_NAME);

CREATE TABLE UM_SYSTEM_USER_ROLE(
    UM_ID INTEGER NOT NULL AUTO_INCREMENT,
    UM_USER_NAME VARCHAR(255),
    UM_ROLE_ID INTEGER NOT NULL,
    UNIQUE (UM_USER_NAME, UM_ROLE_ID),
    FOREIGN KEY (UM_ROLE_ID) REFERENCES UM_SYSTEM_ROLE(UM_ID),
    PRIMARY KEY (UM_ID)
)ENGINE INNODB;