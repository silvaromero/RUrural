CREATE DATABASE rurural;

USE rurural;

CREATE TABLE Usuario (
id INT NOT NULL AUTO_INCREMENT,
rfid INT NOT NULL,
nome VARCHAR(100),
cpf VARCHAR(30),
saldo FLOAT,
PRIMARY KEY(id)
);

CREATE TABLE Rfid (
id INT NOT NULL AUTO_INCREMENT,
tag VARCHAR(20),
PRIMARY KEY(id)
);

ALTER TABLE `Usuario` ADD CONSTRAINT `fk_rfid` FOREIGN KEY ( `rfid` ) REFERENCES `Rfid` ( `id` ) ;

INSERT INTO `Rfid` (`id`, `tag`) VALUES
(1, '123123');

INSERT INTO `Usuario` (`id`, `rfid`, `nome`, `cpf`, `saldo`) VALUES
(1, 1, 'Romero', '12345', 25.5);
