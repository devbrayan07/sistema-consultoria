-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: sistema_consultoria
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `documento`
--

LOCK TABLES `documento` WRITE;
/*!40000 ALTER TABLE `documento` DISABLE KEYS */;
INSERT INTO `documento` (`id_documento`, `tipo`, `nome_arquivo`, `url_arquivo`, `competencia`, `enviado_por`, `criado_em`, `id_empresa`) VALUES (3,'NOTA_FISCAL','Diagrama de Classes.eddx','C:\\Users\\brayan\\Documents\\SISTEMA DE CONSULTORIA JORGE\\sistema-consultoria\\uploads\\1788269882068_Diagrama de Classes.eddx','2026-09-25',5,'2026-09-01 10:38:02',4);
/*!40000 ALTER TABLE `documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `empresa`
--

LOCK TABLES `empresa` WRITE;
/*!40000 ALTER TABLE `empresa` DISABLE KEYS */;
INSERT INTO `empresa` (`id_empresa`, `razao_social`, `nome_fantasia`, `cnpj`, `regime_tributario`, `id_usuario_cliente`, `criado_em`) VALUES (4,'Instituto Virtual Internacional de Mudanças Globais','IVIG','11222333000183','MEI',6,'2026-09-01 13:36:47'),(5,'TESTE','testando','11222333000185','LUCRO_PRESUMIDO',8,'2026-09-01 13:39:29'),(6,'Teste 5','testando 2','12223330001893','SIMPLES_NACIONAL',8,'2026-09-02 14:05:39');
/*!40000 ALTER TABLE `empresa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `empresa_responsavel`
--

LOCK TABLES `empresa_responsavel` WRITE;
/*!40000 ALTER TABLE `empresa_responsavel` DISABLE KEYS */;
/*!40000 ALTER TABLE `empresa_responsavel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `notificacao`
--

LOCK TABLES `notificacao` WRITE;
/*!40000 ALTER TABLE `notificacao` DISABLE KEYS */;
INSERT INTO `notificacao` (`id_notificacao`, `mensagem`, `id_usuario`, `lida`, `referencia_tipo`, `id_referencia`, `criado_em`) VALUES (1,'Nova obrigação DAS lançada para Instituto Virtual Internacional de Mudanças Globais. Vencimento: 2026-09-04',6,0,'OBRIGACAO',1,NULL),(2,'Nova obrigação DAS lançada para IVIG. Vencimento: 2026-10-09',8,0,'OBRIGACAO',2,NULL),(3,'Nova obrigação FOLHA lançada para Instituto Virtual Internacional de Mudanças Globais. Vencimento: 2026-10-03',6,0,'OBRIGACAO',3,NULL),(4,'Nova obrigação DEFIS lançada para TESTE. Vencimento: 2026-10-24',8,0,'OBRIGACAO',4,NULL),(5,'Nova obrigação DARF lançada para Instituto Virtual Internacional de Mudanças Globais. Vencimento: 2026-10-08',6,0,'OBRIGACAO',5,NULL),(6,'Nova obrigação FOLHA lançada para Teste 5. Vencimento: 2026-10-10',8,0,'OBRIGACAO',6,NULL),(7,'Nova obrigação FOLHA lançada para Teste 5. Vencimento: 2026-09-30',8,0,'OBRIGACAO',7,NULL),(8,'Nova obrigação DAS lançada para Instituto Virtual Internacional de Mudanças Globais. Vencimento: 2026-10-10',6,0,'OBRIGACAO',8,NULL),(9,'Nova obrigação DAS lançada para Instituto Virtual Internacional de Mudanças Globais. Vencimento: 2026-10-15',6,0,'OBRIGACAO',9,NULL);
/*!40000 ALTER TABLE `notificacao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `obrigacao_fiscal`
--

LOCK TABLES `obrigacao_fiscal` WRITE;
/*!40000 ALTER TABLE `obrigacao_fiscal` DISABLE KEYS */;
INSERT INTO `obrigacao_fiscal` (`id_obrigacao_fiscal`, `tipo`, `competencia`, `data_vencimento`, `id_empresa`, `valor`, `status`, `data_pagamento`, `criado_em`, `honorario`) VALUES (9,'DAS','2026-09-25','2026-10-15',4,500.00,'PENDENTE',NULL,'2026-09-02 12:31:30',100);
/*!40000 ALTER TABLE `obrigacao_fiscal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `pagamento`
--

LOCK TABLES `pagamento` WRITE;
/*!40000 ALTER TABLE `pagamento` DISABLE KEYS */;
/*!40000 ALTER TABLE `pagamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `relatorio_financeiro`
--

LOCK TABLES `relatorio_financeiro` WRITE;
/*!40000 ALTER TABLE `relatorio_financeiro` DISABLE KEYS */;
/*!40000 ALTER TABLE `relatorio_financeiro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` (`id`, `nome`, `email`, `senha`, `tipo`, `ativo`, `criado_em`) VALUES (2,'Brayan Raimundo Campos','brayan.campos@ivig.coppe.ufrj.br','$2a$10$YXIhXLI.A3tJMaVKOge/jumWbaHPPr6lZqjv96ZpVTgepZxdQNcNq','ADMINISTRADOR',1,'2026-08-31 15:44:24'),(5,'Contador Teste 1','contador.teste@gmail.com','$2a$10$04RK3d96AAQNEldsXrQKruIGvRlEpcjTdpA7WjrfKuU805arABFlu','CONTADOR',1,'2026-08-31 15:53:36'),(6,'Usuário de Teste 1','usuario1@gmail.com','$2a$10$B1Mx09M6asUlbh8WPLeROef0xkE4/c0JZdMerwsuhQSl.tbFJfDze','USUARIO',1,'2026-08-31 15:55:35'),(7,'Administrador','admin@consultoria.com','$2a$10$7WOOcr.QtXOl1XEWWNWVs.oU9gK5rFHMLU0ccGp0Sduk7Zhq3Xgve','CONTADOR',1,'2026-08-31 16:10:04'),(8,'Usuário de Teste 2','usuario2@gmail.com','$2a$10$aNB0bJVCF9jWpAoTMCqCUOsBGX4vCi/EwUqKvl8KF733DMdT4SerW','USUARIO',1,'2026-09-01 08:45:27');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-02 16:00:21
