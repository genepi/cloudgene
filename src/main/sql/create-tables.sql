create table user( 
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
	user_id integer not null references user(id) on delete cascade
);

create table parameter( 
	id			integer not null auto_increment primary key,
	name		varchar(100) not null,
	value		varchar(200) not null,
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

