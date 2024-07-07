use COVSERVER;

-- drop table covs_stat_view if exists;

create table covs_stat_view (
    `id` bigint not null auto_increment primary key,
    `app_id` int not null default 0,
    `user` varchar(64) not null,
    `path` varchar(255) not null,
    `timestamp` datetime(3) not null,
    `end` tinyint not null default 0,
    `category` int not null default 0,
    `params` json not null,
    unique key `key_u1` (`user`, `path`, `timestamp`),
    index `key_n1` (`app_id`),
    index `key_n2` (`end`, `category`)
) ENGINE=InnoDB auto_increment=1 default charset=utf8mb4;
