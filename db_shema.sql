CREATE TABLE User
(
	userID INTEGER PRIMARY KEY,
	Username TEXT,
	FirstName TEXT,
	LastName TEXT,
	Role TEXT,
	Password Text
);

CREATE TABLE conversation
(
    conversationID integer PRIMARY KEY,
    timestamp INTEGER,
    title TEXT,
    createrID INTEGER,
    isGroup INTEGER,
    FOREIGN KEY(createrID) REFERENCES user(userID) ON DELETE SET NULL
);

CREATE TABLE Message
(
	messageID INTEGER PRIMARY KEY,
	conversationID INTEGER,
	timestamp INTEGER,
	authorID INTEGER,
	content TEXT,
    FOREIGN KEY (conversationID) REFERENCES conversation(conversationID) ON DELETE CASCADE
);

CREATE TABLE ParticipantOf
(
	userID INTEGER,
	JoinDate INTEGER,
	conversationID integer,
	canWrite integer default 1,
	PRIMARY KEY (userID, conversationID),
	FOREIGN KEY(userID) REFERENCES user(userID) ON DELETE CASCADE,
	FOREIGN KEY(conversationID) REFERENCES conversation(conversationID) ON DELETE CASCADE
);