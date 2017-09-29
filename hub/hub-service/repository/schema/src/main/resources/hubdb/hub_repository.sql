-- Creating applicationTemplate table
create table applicationTemplate (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    dateModified bigint NOT NULL,
    data JSONB NOT NULL
);

ALTER TABLE applicationTemplate ADD CONSTRAINT pkey_applicationTemplate PRIMARY KEY (id);
ALTER TABLE applicationTemplate ADD CONSTRAINT uk_applicationTemplate_name UNIQUE (name);

-- Creating connectedSite table
create table connectedSite (
    id uuid NOT NULL,
    urn character varying(255) NOT NULL,
    dateModified bigint NOT NULL,
    data JSONB NOT NULL
);

ALTER TABLE connectedSite ADD CONSTRAINT pkey_connectedSite PRIMARY KEY (id);
ALTER TABLE connectedSite ADD CONSTRAINT uk_connectedSite_urn UNIQUE (urn);

-- Creating appInstance tables

create table appInstanceConfig (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    appTemplateId uuid NOT NULL,
    deploymentType character varying(255) NOT NULL,
    creatorUserId UUID NOT NULL,
    baseAppInstanceId UUID,
    baseSavedImageId UUID,
    createdOn BIGINT NOT NULL,
    dateModified bigint NOT NULL,
    siteId uuid NOT NULL,
    data JSONB
);

ALTER TABLE appInstanceConfig ADD CONSTRAINT appInstanceConfig_pkey PRIMARY KEY (id);
ALTER TABLE appInstanceConfig ADD CONSTRAINT appInstanceConfig_name_uk UNIQUE (name);

create table appInstanceState (
  appInstanceId uuid NOT NULL,
  state CHARACTER VARYING(31) NOT NULL,
  url character varying(2047),
  dateModified bigint NOT NULL,
  data JSONB
);

ALTER TABLE appInstanceState ADD CONSTRAINT appInstanceState_appInstanceId_fk FOREIGN KEY (appInstanceId) REFERENCES appInstanceConfig;
ALTER TABLE appInstanceState ADD CONSTRAINT appInstanceState_appInstanceId_uk UNIQUE (appInstanceId);


create table savedImage (
    id uuid NOT NULL,
    appTemplateId uuid NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    tags character varying(255),
    creatorUserId UUID NOT NULL,
    dateCreated BIGINT NOT NULL,
    appCopyId uuid NOT NULL,
    siteId uuid NOT NULL,
    baseImageId uuid,
    state character varying(63) NOT NULL
);

create table hubConfig (
    key CHARACTER VARYING(31) NOT NULL,
    data JSONB NOT NULL
);
ALTER TABLE hubConfig ADD CONSTRAINT hubConfig_pkey PRIMARY KEY (key);


ALTER TABLE savedImage ADD CONSTRAINT savedImage_pkey PRIMARY KEY (id);
ALTER TABLE savedImage ADD CONSTRAINT savedImage_name_uk UNIQUE (name);
