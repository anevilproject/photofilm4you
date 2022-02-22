-- CREATE SCHEMA

CREATE SCHEMA IF NOT EXISTS photofilm;

-- ALTER SCHEMA photofilm OWNER TO USER;

-- DROP OBJECTS

DROP TABLE IF EXISTS photofilm.response_image;
DROP TABLE IF EXISTS photofilm.product_rating;
DROP TABLE IF EXISTS photofilm.rent_items;
DROP TABLE IF EXISTS photofilm.rent;
DROP TABLE IF EXISTS photofilm.reservation;
DROP TABLE IF EXISTS photofilm.item;
DROP TABLE IF EXISTS photofilm.product;
DROP TABLE IF EXISTS photofilm.image;
DROP TABLE IF EXISTS photofilm.response;
DROP TABLE IF EXISTS photofilm.question;
DROP TABLE IF EXISTS photofilm.category;
DROP TABLE IF EXISTS photofilm.model;
DROP TABLE IF EXISTS photofilm.cancellation;
DROP TABLE IF EXISTS photofilm.brand;
DROP TABLE IF EXISTS photofilm."user";
DROP SEQUENCE IF EXISTS photofilm.seq_rent_id;

-- CREATE SEQUENCE

CREATE SEQUENCE photofilm.seq_rent_id START 1;

-- CREATE TABLES

CREATE TABLE photofilm.brand
(
    id      character varying(50) NOT NULL,
    deleted timestamp without time zone,
    name    character varying(50) NOT NULL
);

CREATE TABLE photofilm.cancellation
(
    id           character varying(50) NOT NULL,
    creationdate timestamp without time zone,
    penalization numeric(9, 2),
    status       character varying(10) NOT NULL
);

CREATE TABLE photofilm.category
(
    id       character varying(50) NOT NULL,
    deleted  timestamp without time zone,
    name     character varying(50) NOT NULL,
    parentid character varying(50)
);


CREATE TABLE photofilm.image
(
    id      character varying(50) NOT NULL,
    content bytea                 NOT NULL
);

CREATE TABLE photofilm.item
(
    id           character varying(50) NOT NULL,
    deleted      timestamp without time zone,
    serialnumber character varying(30) NOT NULL,
    status       character varying(15) NOT NULL,
    product_id   character varying(50) NOT NULL
);

CREATE TABLE photofilm.model
(
    id       character varying(50) NOT NULL,
    deleted  timestamp without time zone,
    name     character varying(50) NOT NULL,
    brand_id character varying(50)
);

CREATE TABLE photofilm.product
(
    id          character varying(50) NOT NULL,
    deleted     timestamp without time zone,
    name        character varying(50) NOT NULL,
    dailyprice  numeric(6, 2)         NOT NULL,
    description varchar,
    rating      real,
    category_id character varying(50),
    model_id    character varying(50),
    image_id    character varying(50)
);

CREATE TABLE photofilm.product_rating
(
    id          character varying(50)       NOT NULL,
    comment     varchar(1000),
    rating      integer                     NOT NULL,
    customer_id character varying(50)       NOT NULL,
    product_id  character varying(50)       NOT NULL,
    created     timestamp without time zone NOT NULL
);

CREATE TABLE photofilm.question
(
    id          character varying(50)       NOT NULL,
    message     varchar,
    title       character varying(255)      NOT NULL,
    customer_id character varying(50)       NOT NULL,
    created     timestamp without time zone NOT NULL
);

CREATE TABLE photofilm.rent
(
    id              character varying(50)       NOT NULL,
    created         timestamp without time zone NOT NULL,
    date_from       date                        NOT NULL,
    status          character varying(50)       NOT NULL,
    date_to         date                        NOT NULL,
    totalprice      numeric(8, 2)               NOT NULL,
    cancellation_id character varying(50),
    customer_id     character varying(50)       NOT NULL
);

CREATE TABLE photofilm.rent_items
(
    rent_id character varying(50) NOT NULL,
    item_id character varying(50) NOT NULL
);

CREATE TABLE photofilm.reservation
(
    id      character varying(50) NOT NULL,
    date    date                  NOT NULL,
    item_id character varying(50) NOT NULL
);

CREATE TABLE photofilm.response
(
    id          character varying(50)       NOT NULL,
    message     varchar                     NOT NULL,
    status      character varying(15)       NOT NULL,
    question_id character varying(50)       NOT NULL,
    user_id     character varying(50)       NOT NULL,
    created     timestamp without time zone NOT NULL
);

CREATE TABLE photofilm.response_image
(
    id          character varying(50) NOT NULL,
    image_id    character varying(50) NOT NULL,
    response_id character varying(50) NOT NULL
);

CREATE TABLE photofilm."user"
(
    role     character varying(31) NOT NULL,
    id       character varying(50) NOT NULL,
    email    character varying(50) NOT NULL,
    password character varying(70) NOT NULL,
    date     date,
    address  character varying(100),
    name     character varying(50),
    nif      character varying(15),
    phone    character varying(25),
    surname  character varying(50)
);

-- ADD TABLE PRIMARY KEY CONSTRAINTS

ALTER TABLE ONLY photofilm.brand
    ADD CONSTRAINT pk_brand PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.cancellation
    ADD CONSTRAINT pk_cancellation PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.category
    ADD CONSTRAINT pk_category PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.image
    ADD CONSTRAINT pk_image PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.item
    ADD CONSTRAINT pk_item PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.model
    ADD CONSTRAINT pk_model PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.product
    ADD CONSTRAINT pk_product PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.product_rating
    ADD CONSTRAINT pk_product_rating PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.question
    ADD CONSTRAINT pk_question PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.rent
    ADD CONSTRAINT pk_rent PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.reservation
    ADD CONSTRAINT pk_reservation PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.response_image
    ADD CONSTRAINT pk_response_image PRIMARY KEY (id);

ALTER TABLE ONLY photofilm.response
    ADD CONSTRAINT pk_response PRIMARY KEY (id);

ALTER TABLE ONLY photofilm."user"
    ADD CONSTRAINT pk_user PRIMARY KEY (id);

-- ADD TABLE UNIQUE KEY CONSTRAINTS

ALTER TABLE ONLY photofilm."user"
    ADD CONSTRAINT uk_user_email UNIQUE (email);

ALTER TABLE ONLY photofilm.item
    ADD CONSTRAINT uk_item_serialnumber UNIQUE (serialnumber, product_id);

ALTER TABLE ONLY photofilm.category
    ADD CONSTRAINT uk_category_name UNIQUE (name);

ALTER TABLE ONLY photofilm.brand
    ADD CONSTRAINT uk_brand_name UNIQUE (name);

ALTER TABLE ONLY photofilm.model
    ADD CONSTRAINT uk_model_brand_name UNIQUE (name, brand_id);

ALTER TABLE ONLY photofilm.product
    ADD CONSTRAINT uk_product_model_name UNIQUE (name, model_id);

ALTER TABLE ONLY photofilm.reservation
    ADD CONSTRAINT uk_reservation_item_date UNIQUE (item_id, date);

-- ADD TABLE FOREIGN KEY CONSTRAINTS

ALTER TABLE ONLY photofilm.rent_items
    ADD CONSTRAINT fk_rent_items_rent FOREIGN KEY (rent_id) REFERENCES photofilm.rent (id);

ALTER TABLE ONLY photofilm.product
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES photofilm.category (id);

ALTER TABLE ONLY photofilm.product
    ADD CONSTRAINT fk_product_image FOREIGN KEY (image_id) REFERENCES photofilm.image (id);

ALTER TABLE ONLY photofilm.response_image
    ADD CONSTRAINT fk_response_image_image FOREIGN KEY (image_id) REFERENCES photofilm.image (id);

ALTER TABLE ONLY photofilm.response
    ADD CONSTRAINT fk_response_question FOREIGN KEY (question_id) REFERENCES photofilm.question (id);

ALTER TABLE ONLY photofilm.reservation
    ADD CONSTRAINT fk_reservation_item FOREIGN KEY (item_id) REFERENCES photofilm.item (id);

ALTER TABLE ONLY photofilm.rent
    ADD CONSTRAINT fk_rent_cancellation FOREIGN KEY (cancellation_id) REFERENCES photofilm.cancellation (id);

ALTER TABLE ONLY photofilm.response
    ADD CONSTRAINT fk_response_user FOREIGN KEY (user_id) REFERENCES photofilm."user" (id);

ALTER TABLE ONLY photofilm.item
    ADD CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES photofilm.product (id);

ALTER TABLE ONLY photofilm.model
    ADD CONSTRAINT fk_model_brand FOREIGN KEY (brand_id) REFERENCES photofilm.brand (id);

ALTER TABLE ONLY photofilm.rent
    ADD CONSTRAINT fk_rent_user FOREIGN KEY (customer_id) REFERENCES photofilm."user" (id);

ALTER TABLE ONLY photofilm.product
    ADD CONSTRAINT fk_product_model FOREIGN KEY (model_id) REFERENCES photofilm.model (id);

ALTER TABLE ONLY photofilm.product_rating
    ADD CONSTRAINT fk_product_rating_user FOREIGN KEY (customer_id) REFERENCES photofilm."user" (id);

ALTER TABLE ONLY photofilm.response_image
    ADD CONSTRAINT fk_response_image_response FOREIGN KEY (response_id) REFERENCES photofilm.response (id);

ALTER TABLE ONLY photofilm.category
    ADD CONSTRAINT fk_category_category FOREIGN KEY (parentid) REFERENCES photofilm.category (id);

ALTER TABLE ONLY photofilm.rent_items
    ADD CONSTRAINT fk_rent_items_item FOREIGN KEY (item_id) REFERENCES photofilm.item (id);

ALTER TABLE ONLY photofilm.question
    ADD CONSTRAINT fk_question_user FOREIGN KEY (customer_id) REFERENCES photofilm."user" (id);

ALTER TABLE ONLY photofilm.product_rating
    ADD CONSTRAINT fk_product_rating_product FOREIGN KEY (product_id) REFERENCES photofilm.product (id);

-- INSERT DATA

INSERT INTO photofilm.brand(id, deleted, name)
VALUES ('b7abe3a9-6329-4602-9ab4-a66c1e900aac', null, 'CANON'),
       ('b62ae21a-7d61-4404-ba05-647649b62a05', null, 'SONY'),
       ('93d116c5-27df-4fb3-814f-9145ed198588', null, 'BLACKMAGIC'),
       ('776845d5-9204-4f44-8136-e62e16b870a7', null, 'SENNHEISER'),
       ('86929fa1-dfc2-4e74-bcd6-ef0ebc7a6f5b', null, 'SIGMA'),
       ('ea2f9a10-721b-4932-afdf-ea6b8efdc2fa', null, 'TAMRON'),
       ('58b08947-77b2-4215-b58b-4d1c7a3e670d', null, 'GODOX'),
       ('a9fe8ea4-991f-4b67-a3bf-6f17a0f1c1de', null, 'RODE');

INSERT INTO photofilm.category (id, deleted, name, parentid)
VALUES ('db0fe616-ead1-427f-b809-8bb83835f8c0', null, 'VIDEO', null),
       ('883b8bb8-3f95-4675-8b63-6bb888d5d02e', null, 'AUDIO', null),
       ('04f1d64a-b485-47a5-9ca7-b6ed4bedbf84', null, 'FOTOGRAFIA', null),
       ('afaf61ba-435b-4ed4-9706-c8c29e2649c4', null, 'IL·LUMINACIÓ', null),
       ('822c4840-ca9e-44e1-a3b8-2d96e1fed3c1', null, 'ACCESORIS', null);

INSERT INTO photofilm.model (id, deleted, name, brand_id)
VALUES ('8db6d40c-e7de-46d6-8046-9e5e103f2a17', null, 'EOS Rebel T7', 'b7abe3a9-6329-4602-9ab4-a66c1e900aac'),
       ('9c13e6de-d412-4ce0-a44d-aaecc558fd93', null, 'ME66 Shotgun Mic', '776845d5-9204-4f44-8136-e62e16b870a7'),
       ('8f7aa70a-6e10-431e-a80d-499d5fd09dc5', null, 'A7 III', 'b62ae21a-7d61-4404-ba05-647649b62a05'),
       ('857c15ea-cf4d-43d5-a545-7b2144153513', null, 'FX9', 'b62ae21a-7d61-4404-ba05-647649b62a05'),
       ('9f35522c-12a4-44f7-9e38-98407a047cbd', null, 'Pocket Cinema 6k', '93d116c5-27df-4fb3-814f-9145ed198588'),
       ('25ec4045-a586-4863-a005-0dde66e92ca8', null, '24-70MM F/2.8 VC', 'ea2f9a10-721b-4932-afdf-ea6b8efdc2fa'),
       ('48802bfc-9393-438f-b355-409c44ad10e8', null, 'AD400 Pro', '58b08947-77b2-4215-b58b-4d1c7a3e670d'),
       ('2e472ff0-6b5c-44fe-9a10-2313f8b8e331', null, '35MM F/1.4 ART', '86929fa1-dfc2-4e74-bcd6-ef0ebc7a6f5b'),
       ('e7afee37-3130-419c-ad74-5d42522a2d72', null, 'Wireless Go', 'a9fe8ea4-991f-4b67-a3bf-6f17a0f1c1de');

INSERT INTO photofilm.product (id, deleted, name, dailyprice, description, rating, category_id, model_id)
VALUES ('57600e6f-49b8-4742-be06-4f117d6a4f7d', null, 'Càmera de vídeo BLACKMAGIC Pocket Cinema 6k', 225.00,
        'Càmera amb resolució 6k', 4.5, 'db0fe616-ead1-427f-b809-8bb83835f8c0', '9f35522c-12a4-44f7-9e38-98407a047cbd'),
       ('4bdaf67b-4aff-4c03-bac9-9baaea1b6a0d', null, 'Càmera de fotos CANON EOS Rebel T7', 50.00,
        'Càmera digital que permet capturar amb gran detall i colors vibrants', 4.8,
        '04f1d64a-b485-47a5-9ca7-b6ed4bedbf84', '8db6d40c-e7de-46d6-8046-9e5e103f2a17'),
       ('27de83e1-cdda-4616-bb74-48f2e6dff0d4', null, 'Lent TAMRON 24-70MM F/2.8 VC', 40.00,
        'Lent professional per a càmeres Canon DSLR', 4.6, '04f1d64a-b485-47a5-9ca7-b6ed4bedbf84',
        '25ec4045-a586-4863-a005-0dde66e92ca8'),
       ('06d1e0d8-8f15-4707-be42-3df128dc0c1d', null, 'Lent SIGMA 35MM F/1.4 ART', 75.00,
        'Lent professional per a càmeres Sony E', 4.8, '04f1d64a-b485-47a5-9ca7-b6ed4bedbf84',
        '2e472ff0-6b5c-44fe-9a10-2313f8b8e331'),
       ('dfc2e419-3e29-44a2-937e-4e889134e6aa', null, 'Strobe GODOX AD400 Pro', 62.00,
        'Monollum alimentat per bateria, llum estroboscòpica de flaix per exterior HSS 1/8000', 4.5,
        'afaf61ba-435b-4ed4-9706-c8c29e2649c4',
        '48802bfc-9393-438f-b355-409c44ad10e8'),
       ('37afd5e0-6360-4ae4-902f-7e8e64f1f3d3', null, 'Micròfon RODE Wireless Go', 23.00,
        'Sistema compacte de micròfon, transmissor i receptor sense fils', 4.7, '883b8bb8-3f95-4675-8b63-6bb888d5d02e',
        'e7afee37-3130-419c-ad74-5d42522a2d72');

INSERT INTO photofilm.item(id, deleted, serialnumber, status, product_id)
VALUES ('7272dc88-5a40-4ea5-9d4b-0d28ac5142a9', null, '8f012e07', 'OPERATIONAL',
        '57600e6f-49b8-4742-be06-4f117d6a4f7d'),
       ('81266ac8-c30e-4be1-abd9-38f8fb9e460f', null, '81266ac8', 'BROKEN', '57600e6f-49b8-4742-be06-4f117d6a4f7d'),
       ('753463b2-3359-4831-89f1-720a2d616e87', null, '753463b2', 'OPERATIONAL',
        '4bdaf67b-4aff-4c03-bac9-9baaea1b6a0d'),
       ('bf25b67c-0663-4536-b482-e3d080233141', null, 'bf25b67c', 'OPERATIONAL',
        '4bdaf67b-4aff-4c03-bac9-9baaea1b6a0d'),
       ('ef48317e-053a-4f63-baf1-3cfc47be9ea6', null, 'ef48317e', 'OPERATIONAL',
        '4bdaf67b-4aff-4c03-bac9-9baaea1b6a0d'),
       ('ec81ee29-1405-412c-ac55-ca28452e773a', null, 'ec81ee29', 'OPERATIONAL',
        '27de83e1-cdda-4616-bb74-48f2e6dff0d4'),
       ('dc2c738c-8905-4543-a77d-44b0b0adf308', null, 'dc2c738c', 'OPERATIONAL',
        '27de83e1-cdda-4616-bb74-48f2e6dff0d4'),
       ('a051d911-b830-48ec-b7bb-c25fed708130', null, 'a051d911', 'OPERATIONAL',
        '06d1e0d8-8f15-4707-be42-3df128dc0c1d'),
       ('d5fd3f35-bff7-4d90-8e2e-6bba51212594', null, 'd5fd3f35', 'OPERATIONAL',
        '06d1e0d8-8f15-4707-be42-3df128dc0c1d'),
       ('69bea3dc-14fa-4bd2-b020-87ef78a606b2', null, '69bea3dc', 'OPERATIONAL',
        'dfc2e419-3e29-44a2-937e-4e889134e6aa'),
       ('938505d0-7c04-40b8-a5e3-7f1764eb2d6a', null, '938505d0', 'OPERATIONAL',
        '37afd5e0-6360-4ae4-902f-7e8e64f1f3d3');



INSERT INTO photofilm."user" (role, id, email, password, date, address, name, nif, phone, surname)
VALUES ('ADMIN', 'dab7285d-0c89-43f0-ba78-672f1489dca2', 'admin', 'ISMvKXpXpadDiUoOSoAfww==', '2020-11-25', null, null,
        null, null, null),
       ('ADMIN', 'ff64b36f-c1da-4e4c-a3ac-85911d569ef3', 'admin2', 'yEJY6cOQWaiat32Ebdq5CQ==', '2020-11-26', null, null,
        null, null, null),
       ('ADMIN', 'bcb62421-c942-4f2c-89e7-4a95b47c6296', 'admin3', 'MsrLL5lPa0IYOhMA2aPo1g==', '2020-11-27', null, null,
        null, null, null),
       ('CUSTOMER', 'd2526838-f33e-41eb-930f-7c06d63b3908', 'customer1', '/7xGdfhk4OmquL33oENwEA==', null,
        'Customer One Address, Barcelona', 'Customer', '12345678X', '123-456-7890', 'One'),
       ('CUSTOMER', 'ce2a2db3-081d-486c-8926-7ac828bda3c0', 'customer2', 'XOTRkf0UrIWhRp+4wpt6ew==', null,
        'Customer Two Address, Barcelona', 'Customer', '12345678A', '098-765-4321', 'Two'),
       ('CUSTOMER', '9ac6a8d0-5d0c-40a8-ad98-533505139236', 'customer3', 'Az9/YSFQGumCha138hbV5w==', null,
        'Customer Three Address, Barcelona', 'Customer', '12345678A', '765-123-4321', 'Three');

-- product_rating
INSERT INTO photofilm.product_rating(id, comment, rating, customer_id, product_id, created)
VALUES ('1a40d4d0-f851-4425-8c92-d8a1862b2bb5', 'Una càmera fantàstica. Molt recomanable', 5,
        'd2526838-f33e-41eb-930f-7c06d63b3908', '57600e6f-49b8-4742-be06-4f117d6a4f7d', '2020-12-07 11:35:06'),
       ('eaaa0a78-897e-40df-bbc8-7278ffdc72ad', 'Una càmera fantàstica. Molt recomanable', 5,
        'd2526838-f33e-41eb-930f-7c06d63b3908', '4bdaf67b-4aff-4c03-bac9-9baaea1b6a0d', '2020-12-07 11:37:06'),
       ('520eb05a-5252-4980-84e8-71fc0d78bbe7', 'Una lent increïble', 5, 'ce2a2db3-081d-486c-8926-7ac828bda3c0',
        '27de83e1-cdda-4616-bb74-48f2e6dff0d4', '2020-12-07 11:39:06'),
       ('7edf338e-9e3c-42fb-95cd-033c749a3468', 'Una lent millorable', 3, 'ce2a2db3-081d-486c-8926-7ac828bda3c0',
        '06d1e0d8-8f15-4707-be42-3df128dc0c1d', '2020-12-07 11:40:06');

--question
INSERT INTO photofilm.question(id, message, title, customer_id, created)
VALUES ('f76ece69-6322-4888-ace3-522b5d02ef38', 'Quina mena d''adaptador tenen les lents?', 'adaptador lents',
        '9ac6a8d0-5d0c-40a8-ad98-533505139236', '2020-12-07 11:47:00'),
       ('8c3b7da3-c16b-4bae-bda2-4e6816614cba', 'Quina és la durada mínima d''un lloguer', 'durada lloguer',
        '9ac6a8d0-5d0c-40a8-ad98-533505139236', '2020-12-07 11:57:00'),
       ('57503693-eb23-4166-aa63-e684c8e02fcc', 'fins a quin dia puc cancel·lar un lloguer sense penalització',
        'penalització lloguer', '9ac6a8d0-5d0c-40a8-ad98-533505139236', '2020-12-07 11:58:00'),
       ('178b7847-5641-4ea5-b45c-8dd3f063ead6', 'Teniu algun periode de rebaixes?', 'rebaixes',
        '9ac6a8d0-5d0c-40a8-ad98-533505139236', '2020-12-07 11:59:00'),
       ('5f7e826e-0594-46ce-82c0-31ab628dd400', 'Quins mitjans de pagament són admesos?', 'mitjans de pagament',
        'ce2a2db3-081d-486c-8926-7ac828bda3c0', '2020-12-07 12:01:00');

-- response
INSERT INTO photofilm.response(id, message, status, question_id, user_id, created)
VALUES ('808bb59c-5609-4108-9a2d-823cb28a3817', 'tenim adaptadors per a canon, nikon i sony', 'APPROVED',
        'f76ece69-6322-4888-ace3-522b5d02ef38', 'dab7285d-0c89-43f0-ba78-672f1489dca2', '2020-12-07 12:08:00'),
       ('731d8e7a-4cee-4279-8b6b-80327f5b6ad2', 'la durada mínima d''un lloguer és d''un dia', 'APPROVED',
        '8c3b7da3-c16b-4bae-bda2-4e6816614cba', 'ff64b36f-c1da-4e4c-a3ac-85911d569ef3', '2020-12-07 12:08:00'),
       ('68ee9e90-6765-49e9-b821-bc7d43534a7d', 'fins a 48 hores abans', 'PENDING',
        '57503693-eb23-4166-aa63-e684c8e02fcc', 'd2526838-f33e-41eb-930f-7c06d63b3908', '2020-12-07 12:14:00'),
       ('e5dfda51-5b0d-4570-922d-c02cc5fe88fa', 'sí home, què més voldries!!', 'REJECTED',
        '178b7847-5641-4ea5-b45c-8dd3f063ead6', 'ce2a2db3-081d-486c-8926-7ac828bda3c0', '2020-12-07 12:15:00');
