-- user-service stores users in MongoDB: their ids are strings (ObjectId), not numbers.
-- Existing numeric values are kept as their text form.

alter table cart_item alter column user_id type varchar(255) using user_id::varchar;
alter table orders alter column user_id type varchar(255) using user_id::varchar;
