CREATE TABLE public.attendance_records (
    id uuid NOT NULL,
    approved_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    date date NOT NULL,
    deleted_at timestamp(6) with time zone,
    end_time time(6) without time zone NOT NULL,
    notes character varying(255),
    start_time time(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    approver_id uuid,
    organization_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.attendance_records OWNER TO appuser;

CREATE TABLE public.audit_logs (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    details character varying(2048) NOT NULL,
    event_type character varying(255) NOT NULL,
    ip_address character varying(64),
    user_agent character varying(256),
    user_id uuid
);


ALTER TABLE public.audit_logs OWNER TO appuser;


CREATE TABLE public.leave_requests (
    id uuid NOT NULL,
    approved_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    deleted_at timestamp(6) with time zone,
    end_date date NOT NULL,
    notes character varying(255),
    start_date date NOT NULL,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    approver_id uuid,
    organization_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.leave_requests OWNER TO appuser;

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    entity_id uuid,
    entity_type character varying(255),
    message character varying(2048) NOT NULL,
    sent_at timestamp(6) with time zone,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    recipient_id uuid NOT NULL,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['EMAIL'::character varying, 'WEBSOCKET'::character varying, 'DUMMY'::character varying, 'LEAVE'::character varying, 'ATTENDANCE'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO appuser;

CREATE TABLE public.organizations (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    deleted_at timestamp(6) with time zone,
    name character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    timezone character varying(255) NOT NULL
);


ALTER TABLE public.organizations OWNER TO appuser;

CREATE TABLE public.permissions (
    id uuid NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    deleted_at timestamp(6) with time zone
);


ALTER TABLE public.permissions OWNER TO appuser;

CREATE TABLE public.role_permissions (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO appuser;

CREATE TABLE public.roles (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    deleted_at timestamp(6) with time zone
);


ALTER TABLE public.roles OWNER TO appuser;

CREATE TABLE public.teams (
    id uuid NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    organization_id uuid
);


ALTER TABLE public.teams OWNER TO appuser;

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO appuser;

CREATE TABLE public.users (
    id uuid NOT NULL,
    deleted_at timestamp(6) with time zone,
    email character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    organization_id uuid NOT NULL,
    team_id uuid
);


ALTER TABLE public.users OWNER TO appuser;

CREATE TABLE public.workflow_definitions (
    id uuid NOT NULL,
    description character varying(255),
    entity_type character varying(255) NOT NULL
);


ALTER TABLE public.workflow_definitions OWNER TO appuser;

CREATE TABLE public.workflow_logs (
    id uuid NOT NULL,
    comment character varying(255),
    new_status character varying(255) NOT NULL,
    old_status character varying(255) NOT NULL,
    related_entity_id uuid NOT NULL,
    related_entity_type character varying(255) NOT NULL,
    "timestamp" timestamp(6) with time zone NOT NULL,
    user_id uuid
);


ALTER TABLE public.workflow_logs OWNER TO appuser;

CREATE TABLE public.workflow_step_allowed_roles (
    workflow_step_id uuid NOT NULL,
    role character varying(255)
);


ALTER TABLE public.workflow_step_allowed_roles OWNER TO appuser;

CREATE TABLE public.workflow_step_allowed_transitions (
    workflow_step_id uuid NOT NULL,
    allowed_status character varying(255)
);


ALTER TABLE public.workflow_step_allowed_transitions OWNER TO appuser;

CREATE TABLE public.workflow_steps (
    id uuid NOT NULL,
    condition_expression character varying(255),
    status character varying(255) NOT NULL,
    workflow_definition_id uuid NOT NULL
);


ALTER TABLE public.workflow_steps OWNER TO appuser;

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT attendance_records_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.leave_requests
    ADD CONSTRAINT leave_requests_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.teams
    ADD CONSTRAINT teams_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workflow_definitions
    ADD CONSTRAINT uk3jcsequ2oyhs7ton0tj2a8tlp UNIQUE (entity_type);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.teams
    ADD CONSTRAINT uka510no6sjwqcx153yd5sm4jrr UNIQUE (name);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT ukp9pbw3flq9hkay8hdx3ypsldy UNIQUE (name);

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT ukpnvtwliis6p05pn6i3ndjrqt2 UNIQUE (name);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workflow_definitions
    ADD CONSTRAINT workflow_definitions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workflow_logs
    ADD CONSTRAINT workflow_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workflow_steps
    ADD CONSTRAINT workflow_steps_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workflow_steps
    ADD CONSTRAINT fk1q1hrgxs9klvncdgaokphk2m9 FOREIGN KEY (workflow_definition_id) REFERENCES public.workflow_definitions(id);

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT fk2yka8cp9l26e4kkyab5iyf3ef FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.teams
    ADD CONSTRAINT fk5i52bhmm0nbq6lrbur63anlmc FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

ALTER TABLE ONLY public.workflow_step_allowed_transitions
    ADD CONSTRAINT fkcbgfxwdhi3srf8hk7l7ybqxs FOREIGN KEY (workflow_step_id) REFERENCES public.workflow_steps(id);

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT fkd3tpdy5vhv0gjrwh03pry1uoo FOREIGN KEY (approver_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkegdk29eiy7mdtefy5c7eirr6e FOREIGN KEY (permission_id) REFERENCES public.permissions(id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkfjws1rdruab2bqg7qipoqf65r FOREIGN KEY (team_id) REFERENCES public.teams(id);

ALTER TABLE ONLY public.leave_requests
    ADD CONSTRAINT fkgpv3xa4r19l2wgwk66lruj83v FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

ALTER TABLE ONLY public.leave_requests
    ADD CONSTRAINT fkh6s8bo5d59oy52b6nxfguf4yx FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.workflow_step_allowed_roles
    ADD CONSTRAINT fkhhilh9sfc3qufmmgobd29qwqc FOREIGN KEY (workflow_step_id) REFERENCES public.workflow_steps(id);

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT fkjs4iimve3y0xssbtve5ysyef0 FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkn5fotdgk8d1xvo8nav9uv3muc FOREIGN KEY (role_id) REFERENCES public.roles(id);

ALTER TABLE ONLY public.workflow_logs
    ADD CONSTRAINT fkq8k7t7iir0m0y4bjki545hpnj FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkqpugllwvyv37klq7ft9m8aqxk FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkqqnsjxlwleyjbxlmm213jaj3f FOREIGN KEY (recipient_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT fkr9irc6kd0vax31m17o5b7bviu FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

ALTER TABLE ONLY public.leave_requests
    ADD CONSTRAINT fkrc0m85eup3tao38d99yus0n3j FOREIGN KEY (approver_id) REFERENCES public.users(id);
