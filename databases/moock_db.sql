drop database moockdb;
drop user moock;
create user moock with password 'moock';
create database moockdb with template=template0 owner=moock;
\connect moockdb;
alter default privileges  grant all on tables to moock;
alter default privileges grant all on sequences to moock;

create table mk_users(
user_id integer primary key not null,
first_name varchar(20) not null,
last_name varchar(20),
email varchar(30) not null,
password varchar(255) not null
);

create table mk_categories(
category_id	 integer primary key not null,
parent_category_id integer,
category_name varchar(20) not null,
category_description varchar(50),
category_icon varchar(20)
);

alter table mk_categories add constraint parent_category_fk
foreign key (parent_category_id) references mk_categories(category_id);

create table mk_items(
item_id	 integer primary key not null,
user_id  integer not null,
category_id integer not null,
item_name varchar(20) not null,
item_description varchar(255),
item_price numeric(10,2) not null
);

alter table mk_items add constraint category_fk
foreign key (category_id) references mk_categories(category_id);

alter table mk_items add constraint user_fk
foreign key (user_id) references mk_users(user_id);

create sequence mk_users_seq increment 1 start 1;
create sequence mk_categories_seq increment 1 start 1;
create sequence mk_items_seq increment 1 start 1;
