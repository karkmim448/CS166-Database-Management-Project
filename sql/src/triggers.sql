DROP SEQUENCE IF EXISTS orderid_seq;
CREATE SEQUENCE orderid_seq  START WITH 86655;

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION func_name()
RETURNS "trigger" AS
$BODY$
BEGIN
NEW.orderid = nextval('orderid_seq');
RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS t_name ON Orders;
CREATE TRIGGER t_name BEFORE INSERT 
ON Orders FOR EACH ROW
EXECUTE PROCEDURE func_name();
