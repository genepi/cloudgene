-- 0.0.1

create table `user`( 
	id          integer not null auto_increment primary key,
	username	varchar(100) not null,
	password	varchar(100) not null,
	full_name	varchar(100) null,
	mail		varchar(100) null,
	role		varchar(100) null,
	aws_key	varchar(200) null,
	aws_secret_key	varchar(200) null,
	save_keys boolean,
	export_to_s3 boolean,
	s3_bucket	varchar(200) null,
	export_input_to_s3 boolean,
	activation_code	varchar(200) null,
	active boolean
);

create table job( 
	id		varchar(100) not null primary key,
	state		integer not null,
	start_time	bigint not null,
	end_time	bigint not null,
	name 		varchar(300),
	s3_url		varchar(300),
	type		integer,
	user_id integer not null references `user`(id) on delete cascade
);

create table parameter( 
	id			integer not null auto_increment primary key,
	name		varchar(100) not null,
	`value`		varchar(200) not null,
	type		varchar(25) not null,
	format		varchar(25) null,
	input		boolean,
	download	boolean,
	variable	varchar(100) not null,
	job_id		varchar(100) not null references job(id) on delete cascade
);

create table steps ( 
	id	    	integer not null auto_increment primary key,
	state		integer not null,
	name	    varchar(300),
	start_time	bigint not null,
	end_time	bigint not null,
	job_id		varchar(100) not null
);

create table log_messages(
 	id			integer not null auto_increment primary key, 
	time	    bigint not null,
	type		integer not null,
	message	    varchar(1000),
	step_id		integer not null references steps(id) on delete cascade
);


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
	`value`		bigint not null
);


-- 1.9.0

create table counters_history ( 
	id	    	integer not null auto_increment primary key,
	time_stamp	bigint not null,
	name		varchar(300),
	`value`		bigint not null	
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

alter table `user` add column api_token varchar(300) null default null;

-- 1.19.0

alter table `user` add column login_attempts integer null default 0;
alter table `user` add column locked_until timestamp null default null;
alter table `user` add column last_login timestamp null default null;

-- 1.26.0

alter table job add column submitted_on bigint not null default 0;
alter table job add column finished_on bigint not null default 0;
alter table job add column setup_start_time bigint not null default 0;
alter table job add column setup_end_time bigint not null default 0;

-- 2.0.0-rc3

alter table downloads modify parameter_id INTEGER;
create index idx_downloads_parameter_id on downloads(parameter_id);
create index idx_parameter_job_id on parameter(job_id,input);
create index idx_steps_job_id on steps(job_id);
create index idx_log_messages_step_id on log_messages(step_id);
create index idx_job_user_id on job(user_id,state);

-- 2.0.0
ALTER TABLE log_messages MODIFY COLUMN message VARCHAR (20000);

-- 2.3.0

-- 2.3.4
ALTER TABLE html_snippets MODIFY COLUMN text VARCHAR (8000);

-- 2.3.7
alter table job add column user_agent VARCHAR (400);


-- 2.6.0
alter table `user` add column api_token_expires_on timestamp null default null;