CREATE TABLE PROPERTIES
(
    ID          SERIAL PRIMARY KEY,
    APPLICATION VARCHAR(200),
    PROFILE     VARCHAR(200),
    LABEL       VARCHAR(200),
    PROP_KEY    VARCHAR(200),
    PROP_VALUE  VARCHAR(700),
    CREATED_AT  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create function to auto-update UPDATED_AT timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.UPDATED_AT = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to call the function on UPDATE
CREATE TRIGGER update_properties_updated_at
    BEFORE UPDATE ON PROPERTIES
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

INSERT INTO PROPERTIES (APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE)
VALUES ('config', 'prod', 'main', 'build.id', '101'),
       ('config', 'prod', 'main', 'build.version', '1.2.3'),
       ('config', 'prod', 'main', 'build.name', 'Database-Production-Build SERVER APPLICATION'),
       ('config', 'prod', 'main', 'build.type', 'Database-Production-Build SERVER APPLICATION');

INSERT INTO PROPERTIES (APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE)
VALUES ('config', 'dev', 'main', 'build.id', '100'),
       ('config', 'dev', 'main', 'build.version', '1.2.0'),
       ('config', 'dev', 'main', 'build.name', 'Database-Development-Build SERVER DB'),
       ('config', 'dev', 'main', 'build.type', 'Database-Development-Build SERVER DB');
