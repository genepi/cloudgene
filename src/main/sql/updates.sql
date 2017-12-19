-- 1.0.3

create table downloads(
 	id			  integer not null auto_increment primary key,
 	parameter_id  varchar(200) not null,
 	job_id  varchar(100) not null,
 	name		  varchar(200) null,
 	path		  varchar(200) null,
 	hash          varchar(200) null,		   
	count	      integer not null,
	size		  varchar(200) null
); 

-- 1.0.5

create table cache_entries ( 
	id	    	integer not null auto_increment primary key,
	signature	    varchar(300),
	used		integer not null,
	last_used_on	bigint not null,
	created_on	bigint not null,
	execution_time	bigint not null,
	size	        bigint not null,
	user_id		varchar(100) null,
	job_id		varchar(100) null,
	output          varchar(1000)
);

-- 1.0.7

create table counters ( 
	id	    	integer not null auto_increment primary key,
	name		varchar(300),
	job_id		varchar(100) null,
	value		bigint not null
);


-- 1.9.0

create table counters_history ( 
	id	    	integer not null auto_increment primary key,
	time_stamp	bigint not null,
	name		varchar(300),
	value		bigint not null	
);

-- 1.9.1

create table html_snippets ( 
	id	    	integer not null auto_increment primary key,
	`key`	     	varchar(300),
	text		varchar(1000)	
);

-- 1.9.2

alter table job add column deleted_on bigint null default null;

-- 1.9.3

alter table parameter add column admin_only boolean not null default false;


-- 1.9.6
alter table job add column application varchar(300) null default null;

-- 1.9.8
alter table job add column application_id varchar(300) null default null;


-- 1.16.0

alter table user add column api_token varchar(300) null default null;

-- 1.19.0

alter table user add column login_attempts integer null default 0;
alter table user add column locked_until timestamp null default null;
alter table user add column last_login timestamp null default null;

-- 1.26.0

alter table job add column submitted_on bigint not null default 0;
alter table job add column finished_on bigint not null default 0;
alter table job add column setup_start_time bigint not null default 0;
alter table job add column setup_end_time bigint not null default 0;
